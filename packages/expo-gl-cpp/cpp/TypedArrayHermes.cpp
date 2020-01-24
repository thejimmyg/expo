#include "TypedArrayJSI.h"

#include <list>
#include <hermes/VM/JSTypedArray.h>

namespace vm = hermes::vm;

using Type = TypedArray::Type;

template <Type T> using ContentType = TypedArray::ContentType<T>;

template <Type> struct hermesTypeMap;

#define TYPE_MAP(jsi, hermes) template <> struct hermesTypeMap<Type::jsi> { static constexpr vm::CellKind type = hermes; };
TYPE_MAP(Int8Array, vm::CellKind::Int8ArrayKind);
TYPE_MAP(Int16Array, vm::CellKind::Int16ArrayKind);
TYPE_MAP(Int32Array, vm::CellKind::Int32ArrayKind);
TYPE_MAP(Uint8Array, vm::CellKind::Uint8ArrayKind);
// TYPE_MAP(Uint8ClampedArray, vm::CellKind::Uint8ClampedArrayKind);
TYPE_MAP(Uint16Array, vm::CellKind::Uint16ArrayKind);
TYPE_MAP(Uint32Array, vm::CellKind::Uint32ArrayKind);
TYPE_MAP(Float32Array, vm::CellKind::Float32ArrayKind);
TYPE_MAP(Float64Array, vm::CellKind::Float64ArrayKind);
TYPE_MAP(ArrayBuffer, vm::CellKind::ArrayBufferKind);
// TYPE_MAP(None, nullptr };
#undef TYPE_MAP

template <Type T> constexpr vm::CellKind CellKind() { return hermesTypeMap<T>::type; }

// WARNING: This implementation might break with hermes update
//
// HermesRuntime is a fake implementation that is binary comaptible with
// HermesRuntimeImpl from API/hermes/hermes.cpp. It's used to access internals
// of hermes runtime.
class HermesRuntime : public jsi::Runtime {
public:
  template <typename T>
  class ManagedValues {
  public:
    std::list<T> *operator->() {
      return &values;
    }

    const std::list<T> *operator->() const {
      return &values;
    }

    std::list<T> values;
  };

  class CountedPointerValue : public PointerValue {
  public:
    CountedPointerValue() : refCount(1) {}

    void invalidate() override {
      dec();
    }

    void inc() {
      auto oldCount = refCount.fetch_add(1, std::memory_order_relaxed);
      assert(oldCount + 1 != 0 && "Ref count overflow");
      (void)oldCount;
    }

    void dec() {
      auto oldCount = refCount.fetch_sub(1, std::memory_order_relaxed);
      assert(oldCount > 0 && "Ref count underflow");
      (void)oldCount;
    }

    uint32_t get() const {
      return refCount.load(std::memory_order_relaxed);
    }

  private:
    std::atomic<uint32_t> refCount;
  };

  class HermesPointerValue final : public CountedPointerValue {
  public:
    HermesPointerValue(vm::HermesValue hv) : phv(hv) {}

    const vm::PinnedHermesValue phv;
  };

  // this constructor won't never be called, it's used to remove
  // compiler warnings about uninitialised values.
  HermesRuntime(vm::Runtime* vmRuntime) : rt_(std::shared_ptr<vm::Runtime>(vmRuntime)), runtime_(*vmRuntime) {};

  // fakeVirtualMethod is forcing compiler to create
  // virtual method table that is necessary to keep ABI
  // compatiblity with real JSCRuntime implementation
  virtual void fakeVirtualMethod() {};

  ManagedValues<HermesPointerValue> hermesValues_;
  std::shared_ptr<vm::Runtime> rt_; // supports only 64-bit platforms
  vm::Runtime &runtime_;
};

HermesRuntime* getNativeRuntime(jsi::Runtime& runtime) {
  return reinterpret_cast<HermesRuntime*>(&runtime);
}

class Convert : public jsi::Runtime {
public:
  static jsi::Value toJSI(HermesRuntime* vmRt, vm::HermesValue value) {
    // TODO check if object
    vmRt->hermesValues_->emplace_front(value);
    return jsi::Runtime::make<jsi::Object>(&(vmRt->hermesValues_->front()));
  }

  static vm::Handle<vm::JSObject> toHermes(jsi::Runtime& runtime, const jsi::Value& value) {
    return vm::Handle<vm::JSObject>::vmcast(
        &static_cast<const HermesRuntime::HermesPointerValue *>(jsi::Runtime::getPointerValue(value))->phv);
  }
};

template <Type T> jsi::Value TypedArray::create(jsi::Runtime& runtime, std::vector<ContentType<T>> data) {
  using Array = vm::JSTypedArray<ContentType<T>, CellKind<T>()>;
  HermesRuntime* vmRt = getNativeRuntime(runtime);
  vm::CallResult<vm::HermesValue>&& result = Array::create(&vmRt->runtime_, Array::getPrototype(&vmRt->runtime_));
  return Convert::toJSI(vmRt, result.getValue());
}

void TypedArray::updateWithData(jsi::Runtime& runtime, const jsi::Value& jsValue, std::vector<uint8_t> data) {
}

template <Type T> std::vector<ContentType<T>> TypedArray::fromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
  return std::vector<ContentType<T>>();
}

std::vector<uint8_t> TypedArray::rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
  return std::vector<uint8_t>();
}

Type TypedArray::typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
  vm::CellKind type = Convert::toHermes(runtime, jsVal)->getKind();
  switch (type) {
    case vm::CellKind::Int8ArrayKind:
      return Type::Int8Array;
    case vm::CellKind::Int16ArrayKind:
      return Type::Int16Array;
    case vm::CellKind::Int32ArrayKind:
      return Type::Int32Array;
    case vm::CellKind::Uint8ArrayKind:
      return Type::Uint8Array;
    //case vm::CellKind::Uint8ClampedArrayKind:
    //  return Type::Uint8ClampedArray;
    case vm::CellKind::Uint16ArrayKind:
      return Type::Uint16Array;
    case vm::CellKind::Uint32ArrayKind:
      return Type::Uint32Array;
    case vm::CellKind::Float32ArrayKind:
      return Type::Float32Array;
    case vm::CellKind::Float64ArrayKind:
      return Type::Float64Array;
    case vm::CellKind::ArrayBufferKind:
      return Type::ArrayBuffer;
    default:
      return Type::None;
  }
}

// If templates are defined inside cpp file they need to be explicitly instantiated
template jsi::Value TypedArray::create<TypedArray::Int32Array>(jsi::Runtime&, std::vector<int32_t>);
template jsi::Value TypedArray::create<TypedArray::Uint32Array>(jsi::Runtime&, std::vector<uint32_t>);
template jsi::Value TypedArray::create<TypedArray::Float32Array>(jsi::Runtime&, std::vector<float>);

template std::vector<int32_t> TypedArray::fromJSValue<TypedArray::Int32Array>(jsi::Runtime&, const jsi::Value& jsVal);
template std::vector<uint32_t> TypedArray::fromJSValue<TypedArray::Uint32Array>(jsi::Runtime&, const jsi::Value& jsVal);
template std::vector<float> TypedArray::fromJSValue<TypedArray::Float32Array>(jsi::Runtime&, const jsi::Value& jsVal);
