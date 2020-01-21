#include "TypedArrayJSI.h"

#include <hermes/VM/JSTypedArray.h>

namespace vm = hermes::vm;

using Type = TypedArray::Type;

template <Type T> using ContentType = TypedArray::ContentType<T>;

template <Type> struct hermesTypeMap;

template <> struct hermesTypeMap<Type::Int8Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeInt8Array; };
template <> struct hermesTypeMap<Type::Int16Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeInt16Array; };
template <> struct hermesTypeMap<Type::Int32Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeInt32Array; };
template <> struct hermesTypeMap<Type::Uint8Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeUint8Array; };
template <> struct hermesTypeMap<Type::Uint8ClampedArray> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeUint8ClampedArray; };
template <> struct hermesTypeMap<Type::Uint16Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeUint16Array; };
template <> struct hermesTypeMap<Type::Uint32Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeUint32Array; };
template <> struct hermesTypeMap<Type::Float32Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeFloat32Array; };
template <> struct hermesTypeMap<Type::Float64Array> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeFloat64Array; };
template <> struct hermesTypeMap<Type::ArrayBuffer> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeArrayBuffer; };
template <> struct hermesTypeMap<Type::None> { static constexpr JSTypedArrayType type = kJSTypedArrayTypeNone; };

template <Type T> JSTypedArrayType hermesArrayType() { return hermesTypeMap<T>::type; }

// WARNING: This implementation might break with hermes update
//
// HermesRuntime is a fake implementation that is binary comaptible with
// HermesRuntimeImpl from API/hermes/hermes.cpp. It's used to access internals
// of hermes runtime.
class HermesRuntime : public jsi::Runtime {
public:
  MangedValues<HermesPointerValue> hermesValues_;
  std::shared_ptr<vm::Runtime> rt_; // supports only 64-bit platforms
  vm::Runtime &runtime_;

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
  }

  class CountedPointerValue : public PointerValue {
  public:
    CountedPointerValue() : refCount(1) {}

    void invalidate() override {
      dec();
    }

    void inc() {
      auto oldCount = refCount.fetch_add(1, std::memory_order_relaxed);
      assert(oldCount + 1 != 0 && "Ref count overflow")
      (void)oldCount;
    }

    void dec() {
      auto oldCount = refCount.fetch_sub(1, std::memory_order_relaxed);
      assert(oldCount > 0 && "Ref count underflow")
      (void)oldCount;
    }

    uint32_t get() const {
      refCount.load(std::memory_order_relaxed);
    }

  private:
    std::atomic<uint32_t> refCount;
  };

  class HermesPointerValue final : public CountedPointerValue {
  public:
    HermesPointerValue(vm::HermesValue hv) : phv(hv) {}

    const vm::PinnedHermesValue phv;
  };

  // fakeVirtualMethod is forcing compiler to create
  // virtual method table that is necessary to keep ABI
  // compatiblity with real JSCRuntime implementation
  virtual void fakeVirtualMethod() {}
};

class Convert : public jsi::Runtime {
public:
  static jsi::Value toJSI(HermesRuntime* runtime, vm::HermesValue value) {
    // TODO check if object
    hermesValues_->emplace_front(value);
    return jsi::Runtime::make<jsi::Object>(&(runtime->hermesValues_->front()));
  }

  static vm::HermesValue toJSC(jsi::Runtime& runtime, const jsi::Value& value) {
    return static_cast<const HermesPointerValue *>(jsi::Runtime::getPointerValue(value))->phv;
  }
};

template <Type T> jsi::Value TypedArray::create(jsi::Runtime& runtime, std::vector<ContentType<T>> data) {
  return
}

void TypedArray::updateWithData(jsi::Runtime& runtime, const jsi::Value& jsValue, std::vector<uint8_t> data) {
}

template <Type T> std::vector<ContentType<T>> TypedArray::fromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
}

std::vector<uint8_t> TypedArray::rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
}

Type TypedArray::typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
  auto jsc = getCtxRef(runtime);
  JSTypedArrayType type = JSValueGetTypedArrayType(jsc->ctx, Convert::toJSC(runtime, jsVal), nullptr);
  switch (type) {
    case kJSTypedArrayTypeInt8Array:
      return Type::Int8Array;
    case kJSTypedArrayTypeInt16Array:
      return Type::Int16Array;
    case kJSTypedArrayTypeInt32Array:
      return Type::Int32Array;
    case kJSTypedArrayTypeUint8Array:
      return Type::Uint8Array;
    case kJSTypedArrayTypeUint8ClampedArray:
      return Type::Uint8ClampedArray;
    case kJSTypedArrayTypeUint16Array:
      return Type::Uint16Array;
    case kJSTypedArrayTypeUint32Array:
      return Type::Uint32Array;
    case kJSTypedArrayTypeFloat32Array:
      return Type::Float32Array;
    case kJSTypedArrayTypeFloat64Array:
      return Type::Float64Array;
    case kJSTypedArrayTypeArrayBuffer:
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
