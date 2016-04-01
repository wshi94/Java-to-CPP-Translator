#pragma once

#include <cstring>

#if 0
#include <iostream>
#define TRACE(s) \
  std::cout << __FUNCTION__ << ":" << __LINE__ << ":" << s << std::endl
#else
#define TRACE(s)
#endif

namespace __rt {

  template<typename T>
  struct object_policy;

  template<typename T>
  struct array_policy;

  template<typename T>
  struct java_policy;

  template<typename T, template <typename> class P = java_policy>
  class Ptr {
      T* addr;
      size_t* counter;

  public:
      typedef T value_type;
      typedef P<T> delete_policy;

      Ptr(T* addr = 0) : addr(addr), counter(new size_t(1)) {
        TRACE(addr);
      }

      Ptr(const Ptr& other) : addr(other.addr), counter(other.counter) {
        TRACE(addr);
        ++(*counter);
      }

      ~Ptr() {
        TRACE(addr);
        if (0 == --(*counter)) {
          delete_policy::destroy(addr);;
          delete counter;
        }
      }

      Ptr& operator=(const Ptr& right) {
        TRACE(addr);
        if (addr != right.addr) {
          if (0 == --(*counter)) {
            delete_policy::destroy(addr);
            delete counter;
          }
          addr = right.addr;
          counter = right.counter;
          ++(*counter);
        }
        return *this;
      }

      T& operator*()  const { TRACE(addr); return *addr; }
      T* operator->() const { TRACE(addr); return addr;  }
      T* raw()        const { TRACE(addr); return addr;  }

      template<typename U, template <typename> class Q>
      friend class Ptr;

      template<typename U, template <typename> class Q>
      Ptr(const Ptr<U,Q>& other) : addr((T*)other.addr), counter(other.counter) {
        TRACE(addr);
        ++(*counter);
      }

      template<typename U, template <typename> class Q>
      bool operator==(const Ptr<U,Q>& other) const {
        return addr == (T*)other.addr;
      }

      template<typename U, template <typename> class Q>
      bool operator!=(const Ptr<U,Q>& other) const {
        return addr != (T*)other.addr;
      }

  };

  // Deletion policies

  template<typename T>
  struct object_policy {
      static void destroy(T* addr) {
        delete addr;
      }
  };

  template<typename T>
  struct array_policy {
      static void destroy(T* addr) {
        delete[] addr;
      }
  };

  template<typename T>
  struct java_policy {
      static void destroy(T* addr) {
        if (0 != addr) addr->__vptr->__delete(addr);
      }
  };

}