# Streaming JSON Processor

## VisitJsonProcessor

`VisitJsonProcessor` permits visiting a JSON and applying different transformers on the way.

For now `VisitJsonProcessor` requires you specify an OutputStream. If you only need to consume parts of the JSON using this processor, specify a `OutputStream.nullOutputStream()`

### Json Element Transformers

#### Replace
```
<T> JsonElementTransformer replace(PathMatcher pathMatcher, T replacer)
```

Used to replace an entire JSON element at the given path.

Note: This will replace the value while the field will stay the same

#### Peek
```
<T> JsonElementTransformer peek(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonObjectElement<T>> consumer)
```

Used to read an object at the given location. The object will be written to the OutputStream as is.

Read more about **[JsonObjectElement](#jsonObjectElement)**

#### PeekAll
```
<T> JsonElementTransformer peekAll(PathMatcher pathMatcher, Class<T> clazz, Consumer<JsonArrayElement<T>> consumer)
```

Used to read a list at the given location. The entire list will be written to the OutputStream

### Others

#### JsonElement
`JsonElement` is an interface that is used to hold any type of JsonElement together with it's field name.
Mostly, it is used within different transformers and for writing elements in a JSON.

#### JsonObjectElement

`JsonObjectElement` is an implementation of the `JsonElement` interface.
The element of such a class it's expected to be a non array.

#### JsonArrayElement

`JsonArrayElement` is an implementation of the `JsonElement` interface.
The element of such a class it's expected to be an array of objects.