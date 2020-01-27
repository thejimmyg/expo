#include "TypedArrayJSI.h"
#include <hermes/hermes.h>

#include <list>

using Type = TypedArray::Type;

template <Type T> jsi::Value TypedArray::create(jsi::Runtime& runtime, std::vector<TypedArray::ContentType<T>> data) {
  return reinterpret_cast<facebook::hermes::HermesRuntime*>(&runtime)->createInt32Array(data.size(), reinterpret_cast<int32_t*>(&data[0]));
}

void TypedArray::updateWithData(jsi::Runtime& runtime, const jsi::Value& jsValue, std::vector<uint8_t> data) {
}

template <Type T> std::vector<TypedArray::ContentType<T>> TypedArray::fromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
  return std::vector<TypedArray::ContentType<T>>();
}

std::vector<uint8_t> TypedArray::rawFromJSValue(jsi::Runtime& runtime, const jsi::Value& val) {
  return std::vector<uint8_t>();
}

Type TypedArray::typeFromJSValue(jsi::Runtime& runtime, const jsi::Value& jsVal) {
  return Type::None;
}

// If templates are defined inside cpp file they need to be explicitly instantiated
template jsi::Value TypedArray::create<TypedArray::Int32Array>(jsi::Runtime&, std::vector<int32_t>);
template jsi::Value TypedArray::create<TypedArray::Uint32Array>(jsi::Runtime&, std::vector<uint32_t>);
template jsi::Value TypedArray::create<TypedArray::Float32Array>(jsi::Runtime&, std::vector<float>);

template std::vector<int32_t> TypedArray::fromJSValue<TypedArray::Int32Array>(jsi::Runtime&, const jsi::Value& jsVal);
template std::vector<uint32_t> TypedArray::fromJSValue<TypedArray::Uint32Array>(jsi::Runtime&, const jsi::Value& jsVal);
template std::vector<float> TypedArray::fromJSValue<TypedArray::Float32Array>(jsi::Runtime&, const jsi::Value& jsVal);
