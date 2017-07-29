#define _GNU_SOURCE
#include <errno.h>
#include <limits.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/wait.h>

static char* jvm_dir;

int check(char* prefix, int n) {
  if (n < 0) {
    perror(prefix);
    exit(1);
  } else {
    return n;
  }
}

char* path(char* base) {
  static char path[PATH_MAX];
  snprintf(path, PATH_MAX, "%s/%s", jvm_dir, base);
  return path;
}

void spit_int(char* base, int i) {
  FILE* file = fopen(path(base), "w");
  fprintf(file, "%d\n", i);
  fclose(file);
}

int main(int argc, char **argv) {
  jvm_dir = argv[argc - 1];

  // Start a child process and exit the parent.
  if (check("fork parent", fork()) != 0) exit(0);

  int child = check("fork child", fork());
  if (child == 0) {
    check("setsid", setsid());
    check("execv", execv(argv[1], argv+1));
  } else {
    spit_int("jvm.pid", child);

    int status;
    wait(&status);

    status /= 256;
    if (access(path("status"), F_OK) != -1) {
      spit_int("status", status);
    } else if (status <= 128) { // don't print an error for signals
      fprintf(stderr, "java process exited prematurely with status %d: ", status);
      int i;
      for (i=1; i < argc; i++) {
        fprintf(stderr, "%s ", argv[i]);
      }
      fprintf(stderr, "\n");
    }
  }
}
