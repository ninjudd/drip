#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <setjmp.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <sys/types.h>
#include <termios.h>

jmp_buf env;
char* err_prefix;

// Based on code from https://github.com/nelhage/reptyr

int check(char* prefix, int n) {
  if (n < 0) {
    err_prefix = prefix;
    longjmp(env, errno);
  } else {
    return n;
  }
}

int writeall(int fd, const void *buf, ssize_t count) {
  ssize_t n;
  while (count > 0) {
    n = write(fd, buf, count);
    if (n < 0) {
      if (errno == EINTR) continue;
      return n;
    }
    count -= n;
    buf += n;
  }
  return 0;
}

void proxy(int in, int out, int err) {
  char buf[2048];
  ssize_t count;
  fd_set set;
  int nfds = (out > err ? out : err) + 1;

  while (1) {
    FD_ZERO(&set);
    FD_SET(0, &set);
    FD_SET(out, &set);
    if (err != out) FD_SET(err, &set);

    if (select(nfds, &set, NULL, NULL, NULL) < 0) {
      if (errno == EINTR) continue;
      fprintf(stderr, "Error %d on select\n", errno);
      return;
    }

    if (FD_ISSET(0, &set)) {
      count = check("read in", read(0, buf, sizeof buf));
      if (count == 0) return;
      writeall(in, buf, count);
    }
    if (FD_ISSET(out, &set)) {
      count = check("read out", read(out, buf, sizeof buf));
      if (count == 0) return;
      writeall(1, buf, count);
    }
    if (err != out && FD_ISSET(err, &set)) {
      count = check("read err", read(err, buf, sizeof buf));
      writeall(2, buf, count);
    }
  }
}

int open_pty() {
  int fd = check("posix_openpt", posix_openpt(O_RDWR | O_NOCTTY));

  check("grantpt",  grantpt(fd));
  check("unlockpt", unlockpt(fd));

  return fd;
}

int open_fifo(char* path, int oflag) {
  check("mkfifo", mkfifo(path, 0666));
  return check("open", open(path, oflag));
}

int main(int argc, char **argv) {
  if (argc != 4) {
    fprintf(stderr, "Usage: drip_proxy in out err\n");
    exit(1);
  }

  char* tty_name = ttyname(0);  
  struct termios prev;
  int exit_code = 0;

  int in  = 0;
  int out = 0;
  int err = 0;

  if (setjmp(env) == 0) {
    if (tty_name) {
      check("tcgetcattr", tcgetattr(0, &prev));
      struct termios raw = prev;
      cfmakeraw(&raw);
      check("tcsetattr raw", tcsetattr(0, TCSANOW, &raw));
      
      int pty = open_pty();
      char* pty_name = ptsname(pty);
      
      in = pty;
      check("symlink in", symlink(pty_name, argv[1]));
      
      char* out_name = ttyname(1);
      if (out_name && strcmp(tty_name, out_name) == 0) {
        out = pty;
        check("symlink out", symlink(pty_name, argv[2]));
      }

      char* err_name = ttyname(2);
      if (err_name && strcmp(tty_name, err_name) == 0) {
        err = pty;
        check("symlink err", symlink(pty_name, argv[3]));
      }
    }
    
    if (!in)  in  = open_fifo(argv[1], O_WRONLY);
    if (!out) out = open_fifo(argv[2], O_RDONLY);
    if (!err) err = open_fifo(argv[3], O_RDONLY);
    
    proxy(in, out, err);
  } else {
    exit_code = 1;
  }

  if (tty_name) {
    check("tcsetattr prev", tcsetattr(0, TCSANOW, &prev));
  }

  if (exit_code) perror(err_prefix);

  return exit_code;
}
