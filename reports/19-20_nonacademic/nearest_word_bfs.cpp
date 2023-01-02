#include <iostream>
#include <map>
#include <list>
#include <fstream>
#include <string>
#include <queue>

using namespace std;

struct word_node {
  string word;
  int dist;
  list<struct word_node *> adj; 
  struct word_node *prev;
};

int off_by_one(string &w1, string &w2) {
  int i, len, diff;

  if((len = w1.length())!=w2.length()||&w1==&w2)
    return 0;

  for(i=0, diff=0; i<len; i++)
    if(w1[i]!=w2[i])
      if(++diff == 2)
        return 0;
  return 1;
}

int main(int argc, char **argv) {
  string word, iw1 = string{argv[1]}, iw2 = string{argv[2]};
  list<struct word_node> word_graph;
  queue<struct word_node *> next_level; 
  struct word_node *cur_node, cur_word;
  int len;

  if(argc != 3 || (len = iw1.length())!=iw2.length())
    return -1;

  // loading dictionary
  ifstream dict{"dict.txt"};
  while(dict >> word) {
    if(word.length() == len) {
      cur_word = {
        .word = word,
        .dist = 10000,
        .adj = list<struct word_node *>{}
      };
      word_graph.push_back(cur_word);
      if(word == iw1) {
        auto it = word_graph.end();
        it--;
        next_level.push(&(*it));
        (*it).dist = 0;
        (*it).prev = nullptr;
      }
    }
  }

  // find words 1 character apart
  for(struct word_node &w1: word_graph)
    for(struct word_node &w2: word_graph)
      if(off_by_one(w1.word, w2.word))
        w1.adj.push_back(&w2);

  // bfs
  while(!next_level.empty()) {
    cur_node = next_level.front();
    next_level.pop();

    for(struct word_node *w: cur_node->adj) {
      if(w->dist > cur_node->dist) {
        w->dist = cur_node->dist+1;
        w->prev = cur_node;
        if(w->word == iw2) {
          cout << w->word << " ";
          do {
            cout << cur_node->word << " ";
          } while(cur_node = cur_node->prev);
          cout << endl;
          return 0;
        }
        next_level.push(w);
      }
    }
  }

  cout << "no match found" << endl;
  return -1;
}
