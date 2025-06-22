# 🤖 Android AI App (Kotlin + Jetpack Compose + On-Device LLM)

A lightweight Android application built with Kotlin + Jetpack Compose, featuring on-device AI using **Phi-2** and **TinyLLaVA (TinyLLaMA)**. This project demonstrates how to integrate large language models and vision-language models into mobile apps for intelligent offline processing.

---

##  Application Overview

-  Platform: Android
-  Framework: Kotlin + Jetpack Compose
-  LLMs:
  - Phi-2 — for natural language understanding & generation
  - TinyLLaVA — for multimodal (image + text) inference

---

## Use Cases 

- Text summarization
- Image captioning
- Q&A over images
- Code explanation or generation
- Document scanner with AI assistant
- Language helper or education tool

---

## 🔧 Tech Stack

| Layer        | Tech                                 |
|--------------|--------------------------------------|
| UI           | Kotlin + Jetpack Compose             |
| State        | ViewModel, LiveData                  |
| Inference    | GGML / MLC LLM (for on-device LLMs)  |
| Backend (opt)| Retrofit (if cloud fallback needed)  |
| Vision       | Bitmap → Tensor preprocessing        |
| Native Link  | JNI for model integration            |

---

### M1: Phi-2 (by Microsoft)
- Size: 1.3B parameters
- Format: GGML / MLC
- Use: Language tasks

### M2: TinyLLaVA (TinyLLaMA + vision)
- Size: 1.1B base + visual adapter
- Use: Visual question answering, image captioning

> 

