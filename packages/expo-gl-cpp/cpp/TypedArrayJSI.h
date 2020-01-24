#pragma once

#include <jsi/jsi.h>

namespace jsi = facebook::jsi;

enum VMType {
  JSC,
  Hermes,
};

class TypedArray {
public:
  enum Type {
    Int8Array,
    Int16Array,
    Int32Array,
    Uint8Array,
    Uint8ClampedArray,
    Uint16Array,
    Uint32Array,
    Float32Array,
    Float64Array,
    ArrayBuffer,
    None,
  };

private:
  // associate type of an array with type of a content
  template<Type> struct typeMap;

#define TYPE_MAP(array, cell) template<> struct typeMap<array> { typedef cell type; }
  TYPE_MAP(Int8Array, int8_t);
  TYPE_MAP(Int16Array, int16_t);
  TYPE_MAP(Int32Array, int32_t);
  TYPE_MAP(Uint8Array, uint8_t);
  TYPE_MAP(Uint8ClampedArray, uint8_t);
  TYPE_MAP(Uint16Array, uint16_t);
  TYPE_MAP(Uint32Array, uint32_t);
  TYPE_MAP(Float32Array, float);
  TYPE_MAP(Float64Array, double);
  TYPE_MAP(ArrayBuffer, uint8_t);
#undef TYPE_MAP

public:
  template<Type T>
  using ContentType = typename typeMap<T>::type;

  template <Type T>
  static jsi::Value create(jsi::Runtime& runtime, std::vector<ContentType<T>> data);

  static void updateWithData(jsi::Runtime& runtime, const jsi::Value& val, std::vector<uint8_t> data);

  template <Type T>
  static std::vector<ContentType<T>> fromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  static std::vector<uint8_t> rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

  static Type typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& val);

protected:
  virtual VMType vmName() = 0;
};
