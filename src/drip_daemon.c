#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/ioctl.h>

int check(char* prefix, int n) {
  if (n < 0) {
    perror(prefix);
    exit(1);
  } else {
    return n;
  }
}

char* path(char* dir, char* base) {
  static char path[PATH_MAX];
  snprintf(path, PATH_MAX, "%s/%s", dir, base);
  return path;
}

void spit_int(char* dir, char* base, int i) {
  FILE* file = fopen(path(dir, base), "w");
  fprintf(file, "%d\n", i);
  fclose(file);
}

char* slurp_line(char* dir, char* base) {
  static char buf[PATH_MAX];
  FILE* file = fopen(path(dir, base), "r");
  char* str = fgets(buf, PATH_MAX, file);
  fclose(file);
  return str;
}

int main(int argc, char **argv) {
  char* jvm_dir = argv[argc - 1];

  // Start a child process and exit the parent.
  if (check("fork parent", fork()) != 0) exit(0);

  close(1);
  umask(0);
  check("setsid", setsid());

  int pid = check("fork child", fork());
  if (pid == 0) {
    check("execv", execv(argv[1], argv+1));
  } else {
    spit_int(jvm_dir, "jvm.pid", pid);

    // Set controlling terminal.
    char* tty_name = slurp_line(jvm_dir, "tty");

    int fd = open(tty_name, O_NONBLOCK);
    ioctl(fd, TIOCSCTTY, 0);

    int status;
    waitpid(pid, &status, 0);
    spit_int(jvm_dir, "status", status/256);
  }
}
