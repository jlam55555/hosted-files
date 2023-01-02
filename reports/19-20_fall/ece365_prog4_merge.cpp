#include <cstdlib>
#include <cstring>
#include <fstream>
#include <iostream>
#include <string>

#define NO_MERGE_STRING "*** NOT A MERGE ***"
#define MAX_SIZE 1000

// bottom-up approach: fill board, find best path after completion
char board[MAX_SIZE+1][MAX_SIZE+1];
int main() {
  int w12pos, w12len, w1pos, w1len, w2pos, w2len;
  char w12cur;
  std::string ifn, ofn;

  // get ifstream, ofstream
  std::cout << "Enter input filename: ";
  std::cin >> ifn;
  std::cout << "Enter output filename: ";
  std::cin >> ofn;
  std::ifstream ifs{ifn};
  std::ofstream ofs{ofn};

  std::string w1, w2, w12;
  while(ifs >> w1 >> w2 >> w12) {
    memset(board, 0, (MAX_SIZE+1)*(MAX_SIZE+1));
    **board = 1;
    w1len = w1.length();
    w2len = w2.length();
    w12len = w1len + w2len;

    // fill board
    // could make this bounds tighter, but too lazy for now
    for(w12pos = 0; w12pos < w12len; w12pos++)
      for(w1pos = 0; w1pos < w12len; w1pos++) {
        w2pos = w12pos-w1pos;
        if(w1pos > w1len || w2pos > w2len || !board[w1pos][w2pos]) continue;
        
        w12cur = w12[w12pos];
        if(w1pos < w1len && w1[w1pos] == w12cur)
          board[w1pos+1][w2pos] = 1;
        if(w2pos < w2len && w2[w2pos] == w12cur)
          board[w1pos][w2pos+1] = 1;
      }

    // print board (debugging/show process)
    /*std::cerr << "   " << w2 << std::endl;
    for(int i = 0; i < w1len+1; i++) {
      std::cerr << (i?w1[i-1]:' ') << ' ';
      for(int j = 0; j < w2len+1; j++)
        std::cerr << (char)(board[i][j]+'0');
      std::cerr << std::endl;
    }
    std::cerr << std::endl;*/

    if(!board[w1len][w2len]) {
      ofs << NO_MERGE_STRING << std::endl;
      continue;
    }

    // navigate board
    w1pos = w1.length();
    w2pos = w2.length();
    for(w12pos = w12len-1; w12pos >= 0; w12pos--)
      if(w2pos && board[w1pos][w2pos-1])
        --w2pos;
      else {
        w12[w12pos] ^= 32;
        --w1pos;
      }
    ofs << w12 << std::endl;
  }
}
