#pragma once

#include <stdint.h>
#include <string>
#include "java_lang.h"

using namespace java::lang;

namespace inputs {
  namespace finalPresentationTest {

    //Forward declarations of data layout and vtable
    struct __A;
    struct __A_VT;

    struct __B1;
    struct __B1_VT;

    struct __B2;
    struct __B2_VT;

    struct __C;
    struct __C_VT;

    struct __ALoop;
    struct __ALoop_VT;

    struct __BLoop;
    struct __BLoop_VT;


    //Definition of types equivalent to Java semantics
    typedef __A* A;
    typedef __B1* B1;
    typedef __B2* B2;
    typedef __C* C;
    typedef __ALoop* ALoop;
    typedef __BLoop* BLoop;

    // =======================================================================

    //The data layout for A
    struct __A{

      __A();
      __A_VT* __vptr;
      static __A_VT __vtable;
      String a;
      static void setA(A, String x);
      static void printOther(A, A other);
      static String myToString(A);

      static void __init(A __this);

      static Class __class();

    };

    //The vtable layout for A
    struct __A_VT{

      Class __isa;
      int32_t (*hashCode)(A);
      bool (*equals)(A, Object);
      Class (*getClass)(A);
      String (*toString)(A);
      void (*setA)(A, String);
      void (*printOther)(A, A);
      String (*myToString)(A);

      __A_VT()
        : __isa(__A::__class()),
          hashCode((int32_t(*)(A)) &__Object::hashCode),
          equals((bool(*)(A, Object)) &__Object::equals),
          getClass((Class(*)(A)) &__Object::getClass),
          toString((String(*)(A)) &__Object::toString),
          setA(&__A::setA),
          printOther(&__A::printOther),
          myToString(&__A::myToString)
      {
      }
    };

    // =======================================================================
    //The data layout for B1
    struct __B1{

      __B1();
      __B1_VT* __vptr;
      static __B1_VT __vtable;
      String a;
      String b;

      static void __init(B1 __this);

      static Class __class();

    };

    //The vtable layout for B1
    struct __B1_VT{

      Class __isa;
      int32_t (*hashCode)(B1);
      bool (*equals)(B1, Object);
      Class (*getClass)(B1);
      String (*toString)(B1);
      void (*setA)(B1, String);
      void (*printOther)(B1, A);
      String (*myToString)(B1);

      __B1_VT()
        : __isa(__B1::__class()),
          hashCode((int32_t(*)(B1)) &__Object::hashCode),
          equals((bool(*)(B1, Object)) &__Object::equals),
          getClass((Class(*)(B1)) &__Object::getClass),
          toString((String(*)(B1)) &__Object::toString),
          setA((void(*)(B1, String)) &__A::setA),
          printOther((void(*)(B1, A)) &__A::printOther),
          myToString((String(*)(B1)) &__A::myToString)
      {
      }
    };

    // =======================================================================
    //The data layout for B2
    struct __B2{

      __B2();
      __B2_VT* __vptr;
      static __B2_VT __vtable;
      String a;
      String b;

      static void __init(B2 __this);

      static Class __class();

    };

    //The vtable layout for B2
    struct __B2_VT{

      Class __isa;
      int32_t (*hashCode)(B2);
      bool (*equals)(B2, Object);
      Class (*getClass)(B2);
      String (*toString)(B2);
      void (*setA)(B2, String);
      void (*printOther)(B2, A);
      String (*myToString)(B2);

      __B2_VT()
        : __isa(__B2::__class()),
          hashCode((int32_t(*)(B2)) &__Object::hashCode),
          equals((bool(*)(B2, Object)) &__Object::equals),
          getClass((Class(*)(B2)) &__Object::getClass),
          toString((String(*)(B2)) &__Object::toString),
          setA((void(*)(B2, String)) &__A::setA),
          printOther((void(*)(B2, A)) &__A::printOther),
          myToString((String(*)(B2)) &__A::myToString)
      {
      }
    };

    // =======================================================================
    //The data layout for C
    struct __C{

      __C();
      __C_VT* __vptr;
      static __C_VT __vtable;
      String a;
      String b;
      String c;
      static String myToString(C);

      static void __init(C __this);

      static Class __class();

    };

    //The vtable layout for C
    struct __C_VT{

      Class __isa;
      int32_t (*hashCode)(C);
      bool (*equals)(C, Object);
      Class (*getClass)(C);
      String (*toString)(C);
      void (*setA)(C, String);
      void (*printOther)(C, A);
      String (*myToString)(C);

      __C_VT()
        : __isa(__C::__class()),
          hashCode((int32_t(*)(C)) &__Object::hashCode),
          equals((bool(*)(C, Object)) &__Object::equals),
          getClass((Class(*)(C)) &__Object::getClass),
          toString((String(*)(C)) &__Object::toString),
          setA((void(*)(C, String)) &__A::setA),
          printOther((void(*)(C, A)) &__A::printOther),
          myToString(&__C::myToString)
      {
      }
    };

    // =======================================================================
    //The data layout for ALoop
    struct __ALoop{

      __ALoop();
      __ALoop_VT* __vptr;
      static __ALoop_VT __vtable;
      int i;

      static void __init(ALoop __this, int i);

      static int get(ALoop);
      static Class __class();

    };

    //The vtable layout for ALoop
    struct __ALoop_VT{

      Class __isa;
      int32_t (*hashCode)(ALoop);
      bool (*equals)(ALoop, Object);
      Class (*getClass)(ALoop);
      String (*toString)(ALoop);
      int (*get)(ALoop);

      __ALoop_VT()
        : __isa(__ALoop::__class()),
          hashCode((int32_t(*)(ALoop)) &__Object::hashCode),
          equals((bool(*)(ALoop, Object)) &__Object::equals),
          getClass((Class(*)(ALoop)) &__Object::getClass),
          toString((String(*)(ALoop)) &__Object::toString),
          get(&__ALoop::get)
      {
      }
    };

    // =======================================================================
    //The data layout for BLoop
    struct __BLoop{

      __BLoop();
      __BLoop_VT* __vptr;
      static __BLoop_VT __vtable;
      int i;

      static void __init(BLoop __this, int i);

      static int get(BLoop);
      static Class __class();

    };

    //The vtable layout for BLoop
    struct __BLoop_VT{

      Class __isa;
      int32_t (*hashCode)(BLoop);
      bool (*equals)(BLoop, Object);
      Class (*getClass)(BLoop);
      String (*toString)(BLoop);
      int (*get)(BLoop);

      __BLoop_VT()
        : __isa(__BLoop::__class()),
          hashCode((int32_t(*)(BLoop)) &__Object::hashCode),
          equals((bool(*)(BLoop, Object)) &__Object::equals),
          getClass((Class(*)(BLoop)) &__Object::getClass),
          toString((String(*)(BLoop)) &__Object::toString),
          get(&__BLoop::get)
      {
      }
    };

    // =======================================================================

  }
}

