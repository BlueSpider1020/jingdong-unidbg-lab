# jingdong-unidbg-lab

> Reverse-engineering project for analyzing **JD (Jingdong)** Android app native security libraries (`.so`) with **Unidbg**.  
> This server provides HTTP APIs to reproduce JD's `sign`, `login`, and JNI-based cryptographic operations in an emulated environment.

---

## Overview

The JD (Jingdong) Android app uses native libraries for request signing, encryption, and security checks.  
This project emulates those native libraries using [Unidbg](https://github.com/zhkl0228/unidbg) inside a Java Spring Boot server, allowing you to reproduce the app's internal calculations (e.g., signatures, login body, decryption) for analysis and research.

The implementation is based on:
- **Spring Boot** REST API (`UnidbgServerApplication`)
- **Unidbg** to load and run `libjdbitmapkit.so` (and related `.so` files)
- **JDService** logic wrapping JNI function calls
- REST endpoints under `/jd/*` for interacting with the emulated environment

---

## ⚠️ Disclaimer

This repository is for **research and educational purposes only**.  
Do not use it to violate JD's Terms of Service or local laws.  
You must provide your own **APK** and extract `.so` files — **none are included in this repo**.

---

## Features

- Run JD's native `.so` logic without a real device
- Generate **signatures** used in app requests
- Emulate **login body** creation
- Call **JNI decryption methods**
- Extract **JD-specific parameters** (`jdsgParams`) via exposed HTTP endpoints

---

## Requirements

- JDK 11+
- Maven 3.6+
- IntelliJ IDEA or Eclipse (recommended for development)
- JD Android APK (version `11.0.2` or newer — you must download it yourself)

---

## Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/BlueSpider1020/jingdong-unidbg-lab.git
   cd jingdong-unidbg-lab
