# shortid
Predictable shortid inspired by https://github.com/dylang/shortid

### Usage

```scala
case class ShortID(
  alphabet:   String,
  overflow:   Char,
  padLength:  Int,
  version:    Int,
  reduceTime: Long,
  nodeId:     Option[Int]
)
```

* `alphabet`: The characters used for the encoding
* `overflow`: This char will be added between the seconds and counter if we overflow.
       To prevent any duplicate this char should not be included in the `alphabet`.
* `padLength`: The number of characters used to encode the seconds.
              (with base64 and 5 chars it give us ~34years before we start to overflow).
* `version`: Don't change unless you change the algo or `reduceTime` (Int < alphabet.length).
* `reduceTime`: Ignore all milliseconds before a certain time to reduce the size of the date without sacrificing uniqueness.
        To regenerate `DateTime.now()` and bump the `version`. Always bump the version!
* `nodeId`: If you are using multiple servers use this to make each instance has a unique value (Int < alphabet.length).

### Example

```scala
generator = ShortID(
  alphabet   = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",
  overflow   = '_',
  padLength  = 5,
  version    = 1,
  reduceTime = new Date().getTime(),
  nodeId     = 1
)

generator.generate()
"1100008"
"110000C"
"110000C1"
"110000D"

//Overflow (Add the _ char between the seconds and counter)
generator.encode(12312312312l, 101)
"11DRFCsS_1d"
```
