#include "output.h"
#include "java_lang.h"
#include <iostream>

namespace inputs {
  namespace finalPresentationTest {

    using namespace std;

    //--------------------------------------------------------
    // Class A implementation
    //--------------------------------------------------------

    __A::__A() : __vptr(&__vtable) {}

    void __A::__init (A __this) {
      __this->a = (String) __rt::null();
    }

    void __A::setA(A __this, String x) {
      __this->a = x;
    }

    void __A::printOther(A __this, A other) {
      cout << other->__vptr->myToString(other) << endl;
    }

    String __A::myToString(A __this) {
      return __this->a;
    }

    Class __A::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.A"), (Class) __rt::null());
      return k;
    }

    __A_VT __A::__vtable;

    //--------------------------------------------------------
    // End of Class A implementation
    //--------------------------------------------------------
    //--------------------------------------------------------
    // Class B1 implementation
    //--------------------------------------------------------

    __B1::__B1() : __vptr(&__vtable) {}

    void __B1::__init (B1 __this) {
      __this->b = (String) __rt::null();
    }

    Class __B1::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.B1"), (Class) __rt::null());
      return k;
    }

    __B1_VT __B1::__vtable;

    //--------------------------------------------------------
    // End of Class B1 implementation
    //--------------------------------------------------------
    //--------------------------------------------------------
    // Class B2 implementation
    //--------------------------------------------------------

    __B2::__B2() : __vptr(&__vtable) {}

    void __B2::__init (B2 __this) {
      __this->b = (String) __rt::null();
    }

    Class __B2::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.B2"), (Class) __rt::null());
      return k;
    }

    __B2_VT __B2::__vtable;

    //--------------------------------------------------------
    // End of Class B2 implementation
    //--------------------------------------------------------
    //--------------------------------------------------------
    // Class C implementation
    //--------------------------------------------------------

    __C::__C() : __vptr(&__vtable) {}

    void __C::__init (C __this) {
      __this->c = (String) __rt::null();
    }

    String __C::myToString(C __this) {
      return new __String("still C");
    }

    Class __C::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.C"), (Class) __rt::null());
      return k;
    }

    __C_VT __C::__vtable;

    //--------------------------------------------------------
    // End of Class C implementation
    //--------------------------------------------------------
    //--------------------------------------------------------
    // Class ALoop implementation
    //--------------------------------------------------------

    __ALoop::__ALoop() : __vptr(&__vtable) {}

    void __ALoop::__init(ALoop __this, int i) {
      __this->i = 0;
      __this->i = i;
    }

    int __ALoop::get(ALoop __this) {
      return __this->i;
    }

    Class __ALoop::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.ALoop"), (Class) __rt::null());
      return k;
    }

    __ALoop_VT __ALoop::__vtable;

    //--------------------------------------------------------
    // End of Class ALoop implementation
    //--------------------------------------------------------
    //--------------------------------------------------------
    // Class BLoop implementation
    //--------------------------------------------------------

    __BLoop::__BLoop() : __vptr(&__vtable) {}

    void __BLoop::__init(BLoop __this, int i) {
      __ALoop::__init((ALoop) __this, i);
    }

    int __BLoop::get(BLoop __this) {
      return 10-__this->i;
    }

    Class __BLoop::__class() {
      static Class k = new __Class(__rt::literal("inputs.finalPresentationTest.BLoop"), (Class) __rt::null());
      return k;
    }

    __BLoop_VT __BLoop::__vtable;

    //--------------------------------------------------------
    // End of Class BLoop implementation
    //--------------------------------------------------------

  }
}

namespace __rt
{
  template<>
  java::lang::Class Array<inputs::finalPresentationTest::A>::__class()
  {
    static java::lang::Class k =
       new java::lang::__Class(literal("[Linputs.finalPresentationTest.A;"),
                  java::lang::__Object::__class(),
                  inputs::finalPresentationTest::__BLoop::__class());
    return k;
  }
    template<>
    java::lang::Class Array<inputs::finalPresentationTest::B1>::__class()
    {
      static java::lang::Class k =
         new java::lang::__Class(literal("[Linputs.finalPresentationTest.B1;"),
                    java::lang::__Object::__class(),
                    inputs::finalPresentationTest::__BLoop::__class());
      return k;
    }
      template<>
      java::lang::Class Array<inputs::finalPresentationTest::B2>::__class()
      {
        static java::lang::Class k =
           new java::lang::__Class(literal("[Linputs.finalPresentationTest.B2;"),
                      java::lang::__Object::__class(),
                      inputs::finalPresentationTest::__BLoop::__class());
        return k;
      }
        template<>
        java::lang::Class Array<inputs::finalPresentationTest::C>::__class()
        {
          static java::lang::Class k =
             new java::lang::__Class(literal("[Linputs.finalPresentationTest.C;"),
                        java::lang::__Object::__class(),
                        inputs::finalPresentationTest::__BLoop::__class());
          return k;
        }
          template<>
          java::lang::Class Array<inputs::finalPresentationTest::ALoop>::__class()
          {
            static java::lang::Class k =
               new java::lang::__Class(literal("[Linputs.finalPresentationTest.ALoop;"),
                          java::lang::__Object::__class(),
                          inputs::finalPresentationTest::__BLoop::__class());
            return k;
          }
            template<>
            java::lang::Class Array<inputs::finalPresentationTest::BLoop>::__class()
            {
              static java::lang::Class k =
                 new java::lang::__Class(literal("[Linputs.finalPresentationTest.BLoop;"),
                            java::lang::__Object::__class(),
                            inputs::finalPresentationTest::__BLoop::__class());
              return k;
            }
          }
