# time-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Ftime--kotlin-blue.svg)](https://github.com/KotlinMania/time-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/time-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/time-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/time-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/time-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`time-rs/time`](https://github.com/time-rs/time).

**Original Project:** This port is based on [`time-rs/time`](https://github.com/time-rs/time). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `time-rs/time`

> The text below is reproduced and lightly edited from [`https://github.com/time-rs/time`](https://github.com/time-rs/time). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## time

[![minimum rustc: 1.88.0](https://img.shields.io/badge/minimum%20rustc-1.88.0-yellowgreen?logo=rust&style=flat-square)](https://www.whatrustisit.com)
[![version](https://img.shields.io/crates/v/time?color=blue&logo=rust&style=flat-square)](https://crates.io/crates/time)
[![build status](https://img.shields.io/github/actions/workflow/status/time-rs/time/build.yaml?branch=main&style=flat-square)](https://github.com/time-rs/time/actions)
[![codecov](https://codecov.io/gh/time-rs/time/branch/main/graph/badge.svg?token=yt4XSmQNKQ)](https://codecov.io/gh/time-rs/time)

Documentation:

- [latest release](https://docs.rs/time)
- [main branch](https://time-rs.github.io/api/time)
- [book](https://time-rs.github.io/book)

## Minimum Rust version policy

`time` is guaranteed to compile with the latest stable release of Rust in addition to the two prior
minor releases. For example, if the latest stable Rust release is 1.70, then `time` is guaranteed to
compile with Rust 1.68, 1.69, and 1.70.

The minimum supported Rust version may be increased to one of the aforementioned versions if doing
so provides the end user a benefit. However, the minimum supported Rust version may also be bumped
to a version four minor releases prior to the most recent stable release if doing so improves code
quality or maintainability.

For interoperability with third-party crates, it is guaranteed that there exists a version of that
crate that supports the minimum supported Rust version of `time`. This does not mean that the latest
version of the third-party crate supports the minimum supported Rust version of `time`.

## Contributing

Contributions are always welcome! If you have an idea, it's best to float it by me before working on
it to ensure no effort is wasted. If there's already an open issue for it, knock yourself out.
Internal documentation can be viewed [here](https://time-rs.github.io/internal-api/time).

If you have any questions, feel free to use [Discussions]. Don't hesitate to ask questions — that's
what I'm here for!

[Discussions]: https://github.com/time-rs/time/discussions

## License

This project is licensed under either of

- [Apache License, Version 2.0](https://github.com/time-rs/time/blob/main/LICENSE-Apache)
- [MIT license](https://github.com/time-rs/time/blob/main/LICENSE-MIT)

at your option.

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in
time by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any
additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:time-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`time-rs/time`](https://github.com/time-rs/time). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the time authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`time-rs/time`](https://github.com/time-rs/time) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
