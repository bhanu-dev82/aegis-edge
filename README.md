# 🚨 Aegis-Edge — Offline Disaster Triage, Powered by Gemma 4

**Aegis-Edge** is an open-source, offline-first disaster triage agent for Android. It runs **Gemma 4 E2B** entirely on-device via **LiteRT-LM** with GPU acceleration — no internet required.

Point your camera at a patient, describe symptoms by voice or text, and get instant, protocol-driven triage classification using the **START Triage Protocol**.

> **Built for the [Gemma 4 Good Hackathon](https://kaggle.com) • Global Resilience Track**

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🧠 **Thinking Mode** | Gemma 4's native `<\|think\|>` tokens force Chain-of-Thought reasoning before classification |
| 📸 **Multimodal Input** | Attach up to 4 photos + voice recording + text symptoms |
| 🎯 **Constrained JSON Output** | Model outputs strict `{"color":"RED","action":"...","reasoning":"..."}` — no hallucinated chat |
| 📊 **Sorted Triage Queue** | Color-coded patient cards: RED → YELLOW → GREEN → BLACK |
| ✈️ **100% Offline** | Works in airplane mode. Zero data leaves the device |
| ⚡ **GPU Accelerated** | Samsung S24: 3808 tk/s prefill, 52 tk/s decode via LiteRT-LM GPU backend |
| 🎙️ **Voice Notes** | Record 16kHz WAV audio for hands-free patient description |
| ⚡ **Quick Actions** | One-tap follow-up: "How to treat", "First aid supplies needed", etc. |

---

## 📱 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/9ddfb067-ed0d-40ad-b1bd-8c5b3e8cf157" width="220" />
  <img src="https://github.com/user-attachments/assets/a94cdb64-9300-48c8-b7ef-7c6bb7567b57" width="220" />
  <img src="https://github.com/user-attachments/assets/d9f6ac59-0127-47f3-812e-3f77b379bad1" width="220" />
  <img src="https://github.com/user-attachments/assets/87fed076-8efc-48c1-9e2b-5d3dee884364" width="220" />
</p>


*demo video available on YouTube*

---

## 🏥 START Triage Protocol

Aegis-Edge implements the **Simple Triage and Rapid Treatment (START)** protocol:

| Color | Priority | Criteria |
|-------|----------|----------|
| 🔴 RED | Immediate | Breathing >30/min, no radial pulse, capillary refill >2s, can't follow commands |
| 🟡 YELLOW | Delayed | Stable vitals, can follow commands, injuries need treatment but can wait |
| 🟢 GREEN | Minor | Walking wounded, minor injuries |
| ⚫ BLACK | Deceased | Not breathing after airway opened |

---

## 🔧 Setup

### Prerequisites
- Android Studio Hedgehog+
- Samsung Galaxy S24 (or any device with 8GB+ RAM)
- Gemma 4 E2B model file (`.litertlm`)

### Build & Run

```bash
# Clone
git clone https://github.com/YOUR_USERNAME/aegis-edge.git
cd aegis-edge

# Build
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Model Setup

Download the Gemma 4 E2B model and ensure it's accessible to the app:

```bash
# Option 1: The app downloads Gemma 4 E2B automatically from HuggingFace
# Option 2: Push manually via ADB
adb push gemma-4-E2B-it.litertlm /data/local/tmp/
```

---

## 🏗️ Architecture

```
app/src/main/java/com/.../
├── feature/triage/
│   ├── TriageDashboardScreen.kt   # Main UI — input panel + patient queue
│   ├── TriageViewModel.kt         # Engine management + JSON parsing
│   └── TriagePatient.kt           # Data model + TriageColor enum
├── core/llm/
│   ├── LiteRTEngineManager.kt     # LiteRT-LM Engine singleton
│   ├── SystemPrompts.kt           # START Protocol prompt engineering
│   └── ModelRouter.kt             # Optimal model selection
└── core/theme/                    # Material 3 theming + triage colors
```

### Key Technical Decisions

1. **Thinking Mode for Safety**: Every triage call enables `<|think|>` tokens, forcing the model to reason through the START protocol before classifying. This prevents snap-judgment hallucinations.

2. **Structured JSON Output**: The system prompt constrains Gemma 4 to output strict JSON. The ViewModel parses this with a robust fallback — if JSON parsing fails, the patient still gets added to the queue with a YELLOW (cautious) classification.

3. **GPU Backend**: LiteRT-LM initializes with `Backend.GPU()` for real-time inference. Vision backend also uses GPU. Audio uses CPU (required by the audio decoder).

4. **One Model At A Time**: `LiteRTEngineManager` enforces single-model loading to prevent OOM on 8GB devices.

---

## 🤝 Contributing

Contributions welcome! Especially:
- **Internationalization** — translate triage prompts for non-English disaster zones
- **Protocol expansion** — add JumpSTART (pediatric), SALT, or other triage protocols
- **Offline maps integration** — show nearest medical facilities
- **Patient export** — CSV/PDF export of triage queue for handoff to medical teams

---

## ⚠️ Important AI & Medical Disclaimer

**Aegis-Edge is NOT a medical device, and it does not provide clinical diagnoses.** 
It is an experimental AI decision-support tool designed strictly for educational and emergency-preparedness purposes during mass casualty scenarios where professional medical care is unavailable. 

- **No Clinical Diagnoses:** The AI provides "Initial Assessments" based on bystander input. It cannot and does not diagnose medical conditions.
- **AI Limitations:** Aegis-Edge is powered by a Large Language Model (Gemma 4). It can hallucinate, make mistakes, or provide inaccurate information. 
- **Human in the Loop:** Always defer to trained medical professionals. AI guidance should never override human clinical judgment.
- **Safety Policy Compliance:** This project adheres to Google's Generative AI Prohibited Use Policies by explicitly avoiding the provision of formal medical advice or diagnoses.
---

## 📄 License

Apache License 2.0 — see [LICENSE](LICENSE)

---

## 🙏 Acknowledgments

- **Google** — Gemma 4 model family & LiteRT-LM SDK
- **Chakuli Project** — Core LiteRT-LM integration architecture
- **START Protocol** — Newport Beach Fire Department (1983)

---

*Built with ❤️ for humanity. When the network goes down, Aegis-Edge stays up.*
