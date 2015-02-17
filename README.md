# shortid
Predictable shortid inspired by https://github.com/dylang/shortid

### Usage

```scala
case class ShortID(
  alphabet:   String,
  overflow:   Char,
  limit:      Long,
  version:    Int,
  reduceTime: Long,
  nodeId:     Option[Int]
)
```

* `alphabet`: The characters used for the encoding 
* `overflow`: This char will be added between the seconds and counter if we overflow.
       To prevent any duplicate this char should not be included in the `alphabet`.
* `limit`: The ceiling value after which the seconds will overflow
    (2^30 give us ~30years since reduceTime and an encoding length of 5chars with base64).
* `version`: Don't change unless you change the algo or `reduceTime` (Int < alphabet.length).
* `reduceTime`: Ignore all milliseconds before a certain time to reduce the size of the date without sacrificing uniqueness.
        To regenerate `DateTime.now()` and bump the `version`. Always bump the version!
* `nodeId`: If you are using multiple servers use this to make each instance has a unique value (Int < alphabet.length).

### Example

```scala
generator = ShortID(
  alphabet   = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-",
  overflow   = '+',
  limit      = math.pow(2, 30).toLong,
  version    = 1,
  reduceTime = new Date().getTime(),
  nodeId     = 1
)

generator.generate() 
"1100006"
"1100009"
"11000091"
"110000f"

//Overflow
"114Nyq8YqQ8+1u"
```
