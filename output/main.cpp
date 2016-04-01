#include <iostream>
#include <iomanip>
#include <limits>
#include "output.h"
#include "java_lang.h"

using namespace std;
using namespace java::lang;
using namespace inputs::finalPresentationTest;

int main(int argc, char* args[]) {

  cout << "Hello world!" << endl;

  A a = new __A();
  a->__init(a);
  a->__vptr->setA(a, new __String("A"));
  B1 b1 = new __B1();
  b1->__init(b1);
  b1->__vptr->setA(b1, new __String("B1"));
  B2 b2 = new __B2();
  b2->__init(b2);
  b2->__vptr->setA(b2, new __String("B2"));
  C c = new __C();
  c->__init(c);
  c->__vptr->setA(c, new __String("C"));
  a->__vptr->printOther(a, (A) a);
  a->__vptr->printOther(a, (A) b1);
  a->__vptr->printOther(a, (A) b2);
  a->__vptr->printOther(a, (A) c);
  NegativeArraySizeException::Check_Not_Negative( 10 );
  __rt::Array<Object>* as = (__rt::Array<Object>*) new __rt::Array<ALoop>(10);
  __rt::checkNotNull(as);
  __rt::checkIndex(as, 9);

  for(int i = 0; i < as->length; i++){

      __rt::checkNotNull(as);
      __rt::checkIndex(as, i);
      BLoop a = new __BLoop();
      a->__init(a, i);
      __rt::checkStore(as, a);
      as->__data[i] = (Object) a;
    }

    int k = 0;
  
    while(k < 10){

            __rt::checkIndex(as, k);
cout << ((ALoop) as->__data[k])->__vptr->get(((ALoop) as->__data[k])) << endl;

      k = k + 1;
    
    }

    return 0;

  }
