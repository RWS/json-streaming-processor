# Streaming JSON Processor

## VisitJsonProcessor

`VisitJsonProcessor` permits visiting a JSON and applying different transformers on the way.

For now `VisitJsonProcessor` requires you specify an OutputStream. If you only need to consume parts of the JSON using this processor, specify a `OutputStream.nullOutputStream()`

### Json Element Transformers

#### Replace
```
<T> JsonElementTransformer replace(PathMatcher pathMatcher, T replacer)
```