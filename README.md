🤖 Freely — Offline Android AI Chat with Multimodal Support
A privacy-first Android app built in Kotlin + Jetpack Compose, powered by on-device LLMs and MediaPipe's .task format. It features natural language chat and vision-language inference — all running fully offline, right on your device.

📱 App Overview
Platform: Android (Jetpack Compose)

AI Runtime: MediaPipe LLM Inference (.task models)

Modes:

M1 – Text-only chat with Gemma LLM

M2 – Multimodal chat (Image + Text) using visual adapters

Offline-first: No internet required for inference

Cloud-free: No backend servers needed for AI features

🧠 Models Used
Mode	Model File	Description
M1	gemma3-1b-it-int4.task	Lightweight LLM for text chat
M2	gemma-3n-E2B-it-int4.task	Multimodal LLM with image + text support

All models are in .task format (MediaPipe), optimized for mobile GPU or CPU inference.

🔥 Features
✨ Chat with AI (Text & Image input)

📷 Upload images for analysis, captioning, or Q&A

🚀 Fast on-device inference with low memory use

📩 Planned: 3-phase Gmail ↔ Telegram workflow pipeline

🔐 Fully offline — no cloud APIs used

⚙️ Tech Stack
Layer	Technology
UI	Kotlin + Jetpack Compose
Auth	Firebase Authentication (Email + Google Sign-In)
Model	MediaPipe .task inference
Vision Input	BitmapImageBuilder + MediaPipe MPImage
Workflow	Planned: Gmail ↔ Telegram ↔ LLM
Storage	Firebase Firestore (for user metadata)

🚧 Planned Gmail ↔ Telegram Integration (Workflow Engine)
A 3-phase AI-enhanced workflow engine:

Input → Processing → Output
Supported directions:

Gmail ➝ Telegram

Telegram ➝ Gmail

Gmail ➝ Gmail

Telegram ➝ Telegram

Logic Layer: Future versions will use on-device NLP for auto-replies, filtering, or summarization.

🛠️ Setup
Clone the repo

Add .task models to /data/local/tmp/ or use in-app downloader

Set up Firebase Authentication (optional)

Build and run!

⚠️ Notes for Mobile Compatibility
Multimodal model (gemma-3n-E2B-it-int4.task) may require:

Lower maxTokens (e.g., 128)

CPU fallback on low-end devices (e.g., Samsung A32)

Resized image input (224x224) to prevent crashes

🧪 Tested On
✅ Pixel 7 (Android 14) — both models

⚠️ Samsung A32 (4GB RAM) — M1 stable, M2 limited by memory

📄 License
MIT. Attribution for MediaPipe and model authors (Google, DeepMind, etc.) applies.
