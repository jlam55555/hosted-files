#include <stdio.h>
#include <stdlib.h>

#define MAX(a, b) (a>b?a:b)
#define MIN(a, b) (a<b?a:b)

int main(int argc, char **argv) {
  int tot = 0, currentHeight = 0, h1, h2;
  char **bar1 = argv, **bar2 = argv+argc-1;

  while(bar1 != bar2) {
    h1 = atoi(*bar1);
    h2 = atoi(*bar2);
    if(h1 < h2) {
      currentHeight = MAX(currentHeight, h1);
      tot += MAX(currentHeight-h1, 0);
      bar1++;
    } else {
      currentHeight = MAX(currentHeight, h2);
      tot += MAX(currentHeight-h2, 0);
      bar2--;
    }
  }

  dprintf(1, "total water: %d\n", tot);
  return 0;
}
