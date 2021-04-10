[![](https://jitpack.io/v/mtzhisham/KLQuery.svg)](https://jitpack.io/#mtzhisham/KLQuery)


### Download

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

```groovy
	dependencies {
	               implementation 'com.github.mtzhisham.KLQuery:annotation:0.0.17'
                   implementation 'com.github.mtzhisham.KLQuery:util:0.0.17'
                   kapt 'com.github.mtzhisham.KLQuery:codegen:0.0.17'
	}
```

### Usage

1. In any data class: 

```kotlin
@KLQuery
data class Characters(
    val results: List<Result>
)

@KLQuery
data class Result(
    val id: String,
    val name: String
)
```

2. Then write a query 

```kotlin
val query = charactersQuery {
      resultsQuery {
          id
          name
      }
}
```

3. Then get the string 

```kotlin
query.buildQueryString()
``` 

## License

```
MIT License

Copyright (c) 2021 Moataz Hisham

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
