# Versible

A small version and version range comparison library.

## Quick How-To

This section describes how to get started using the library. If you are only interested in the version scheme description skip forward to the Motivation section.

### How To Install

Snapshot builds are available on my personal maven repository at `https://dogforce-games.com/maven/`.

```
repositories {
    maven {
        url 'https://www.dogforce-games.com/maven/'
    }
}
```

The group id is `dev.gigaherz.versible`, the artifact id is `Versible`, and the current version is `1.0-SNAPSHOT`.

```
dependencies {
    implementation 'dev.gigaherz.versible:Versible:1.0-SNAPSHOT'
}
```

### How To Use

Quick reference list:

  * How to parse a version: Call the `VersibleParser.parseVersion("string")` method.
  * How to parse a range: Call the `VersibleParser.parseRange("string")` method.
  * How to compare two versions for ordering: Use the `VersibleVersion#compareTo` method, such as in `version.compareTo(other)`.
  * How to compare two version objects for (strict) equality: Use the `VersibleVersion#equals` method, such as in `version.equals(other)`.
  * How to check if a version is contained in a range: Use the `VersibleRange#contains` method, such as in `range.contains(version)`.
  * How to construct a version object in code: Call the `VersibleVersion.of(...)` method. This method accepts varying parameters which can be numbers, strings, or single characters.
  * How to construct a version range in code: Call one of the static factory methods in the `VersibleRange` class, such as `VersibleRange.between(a,b)`.
  
A temporary javadoc location is [here](http://dogforce-games.com/versible/javadoc/dev/gigaherz/versible/VersibleParser.html) (not ensured to always be up to date with the code in this repository).

### How To Modify / Test / Contribute

Building this library from source is easy. Just clone the repository or download a revision zip, and run the `./gradlew build` command (`gradlew build` without the dot-slash if you use old-fashioned `cmd.exe` as your command prompt).

Contribution follows standard `github` practices: Make a personal fork of the repository, clone, create a work branch, make your changes, commit and push, then make a pull request.

## Motivation

Most versioning systems try to ascribe meaning to the components of a version number. They define major versions as breaking changes, minor versions as additional features, or things like that.

Meanwhile, most people don't even care what the version says. So far as the average user is concerned if a more significant number changes they are more out of date than if a less significant number does.

On top of that, version testing often has quirks that make things unintuitive or inconsistent, specially in the ordering and equivalence.

So I simply was annoyed one afternoon and decided that since I have opinions about this, I might as well put them into code.

## Design

### Versions

A version number shall be a string containing groups of numbers or letters, optionally separated by dots. Each grouping of numbers will be considered a numeric component, and each grouping of letters an alphabetic component. It is not necessary to include dots between numeric and alphabetic components, and the presence of a dot will not change what version is parsed from that string.

A version number will be compared left to right, in a component by component basis. Numeric components will be compared by their numerical value, while alphabetic components will be compared lexicographically. Numeric components will alays sort as greater than alphabetic components.

A version number can be suffixed by either a negative, or a positive tag. Tags are another version string appended at the end, after a '-' for negative tags, and '+' for positive ones.

Negative tags will be sorted immediately before the parent. Their primary purpose is to specify pre-release versioning, such as alpha or beta markers.

Positive tags will be sorted immediately after the parent. Their primary purpose is to specify build metadata, and is often not considered part of the version *per se*. However, for the sake of consistent sorting, all version components are to be considered.

### Ranges

A version range can be expressed in various ways, depending on the user's needs.

The following are considered valid for the purposes of this specification:

  * **A single version**: A range can be a single version string, which will be considered to match only the given version *approximately* (meaning, `1.0` matches the range `[1.0,1.1)`).
  * **An interval**: A range can use the interval notation, where square brackets `[]` denote a closed (includive) end, and parentheses `()` denote an open (exclusive) end. Eg. `[1,2)` will include versions that compare greater than `1` up to and excluding `2`. Intervals can be single-ended on either end (eg. `[1.0,)` or `(,2.0]`), which includes any possible version beyond the specified end.
  * **A comparison**: A range can specify a single-ended interval by specifying one of the comparison operators `>=` (at least), `>` (greater than), `<=` (at most), `<` (lesser than). Eg. `>=1.0.2` is equivalent to the single-ended interval `[1.0.2,)`.
  * **An equivalence**: Matches a single version **exactly**.
  * **A wildcard**: A version can be specified with a wildcard `.*` at the end. This will match all version numbers that are included in the range from the version string before the wildcard, to the version string of the same component length that would sort immediately after. Eg. `1.0.*` will be equivalent to `[1.0,1.1)` and include in it versions with more components, such as `1.0.0` and `1.0.35`.

## Grammar

### Versions

The general grammar for the version string is as follows:

```bnf
<version> ::= <component-list> <suffix>*
<suffix> ::= [+-] <component-list>
<component-list> ::= <component> ('.'? <component>)*
<component> ::= <number> | <word>
<number> ::= {Digit}+
<word> ::= {Letter}+
```

The `{Digit}` character set includes all unicode characters considered to be numeric digits, but this specification strongly recommends using only the characters `'0'` through `'9'`.

The `{Letter}` character set includes all unicode characters considered to be letters, but this specification strongly recommends using only the lower-case latin characters without diacritics, `'a'` through `'z'`.

The lexing is assumed to be greedy, meaning that a sequence of numbers or letters will always be considered one component, and it will never be arbitrarily split in two. As such, the component list `[1,0]` will be considered an **incorrect parsing** of `10`, but a valid parsing of `1.0`.

### Ranges

The general grammar for the range string is as follows:

```bnf
<range> ::= <version>
         |  <version> '.' '*'
         |  <comparison-operator> <version>
         |  <interval>
         
<comparison-operator> ::= '>=' | '>' | '<=' | '<' | '='

<interval>       ::= <left-interval> (<version> ',' <version>? | ',' <version>) <right-interval>
<left-interval>  ::= '[' | '('
<right-interval> ::= ']' | ')'
```

The `<version>` rule is as described in the Versions grammar above.

## API

This library proposes a reference implementation of the above principles.

The `VersibleParser` class is the main entry point of the library. It exposes the `parseVersion(String)` and `parseRange(String)` methods, which parse a version or a range from string, respectively.

The `VersibleParser.parseVersion` method returns a `VersibleVersion` object. This data class contains a sequence of components which can be obtained via either `size()` and `get(int)`, or the `stream()` method. It implements the `compareTo(VersibleVersion)` method from the `Comparable` interface, along with `equals` and `hashCode`. The `compareTo` method satisfies all the sorting requirements from the specification above. 

The `VersibleComponent` interface represents a single component of the version. For the purposes of this API, this also includes the suffixes `'-'` and `'+'`. Three implementations of this interface exist in the API, the `Numeric`, `Alphabetic`, and `Suffix` types, named according to the component type they represent.

The `VersibleParser.parseRange` method returns a `VersibleRange` object. This data class contains a pair of endpoint versions (`minVersion` and `maxVersion`, along with their inclusivity. If the `minVersion` field is non-`null`, the `minExclusive` field determines if this end is inclusive (`false`) or exclusive (`true`). If the `maxVersion` field is non-`null`, the `maxExclusive` field determines if this end is inclusive (`false`) or exclusive (`true`). This class implements a `contains(VersibleVersion)` method, along with an implementation of the `Predicate` interface for convenience.
