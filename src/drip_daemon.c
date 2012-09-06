#include <errno.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

int check(char* prefix, int n) {
  if (n < 0) {
    perror(prefix);
    exit(1);
  } else {
    return n;
  }
}

#define LEN 1024

void spit_int(char* dir, char* base, int i) {
  static char buf[LEN];
  snprintf(buf, LEN, "%s/%s", dir, base);

  FILE* file = fopen(buf,"w");
  fprintf(file,"%d\n", i);
  fclose(file);
}

int main(int argc, char **argv) {
  char* jvm_dir = argv[argc - 1];

  // Start a child process and exit the parent.
  if (check("fork parent", fork()) != 0) exit(0);

  /* close(0); */
  /* close(1); */
  umask(0);

  int pid = check("fork child", fork());
  if (pid == 0) {
    check("setsid", setsid());
    check("execv", execv(argv[1], argv+1));
  } else {
    spit_int(jvm_dir, "jvm.pid", pid);

    int status;
    waitpid(pid, &status, 0);
    spit_int(jvm_dir, "status", status);
  }
}
