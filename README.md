# AI-Email-Writer

An intelligent email reply generator that uses Google's Gemini AI to automatically compose professional email responses. This project consists of a Chrome Extension frontend and a Spring Boot backend.

## Project Overview

The AI-Email-Writer is a full-stack application that integrates with Gmail to provide AI-powered email reply generation. Users can select an email, click the "AI Reply" button, and the application will automatically generate a professional response using Google's Gemini 2.5 Flash model.

---

## Architecture

The project is divided into two main components:

### 1. **email-writer-ext** (Chrome Extension)
A content script-based Chrome extension that integrates directly with Gmail's web interface.

### 2. **email-writer-sb** (Spring Boot Backend)
A REST API server that handles communication with Google's Gemini AI to generate email replies.

---

## How It Works

### User Flow

```
User opens Gmail
    ↓
User clicks on an email to read it
    ↓
Chrome Extension detects the compose window
    ↓
Extension injects "AI Reply" button into Gmail's compose toolbar
    ↓
User clicks "AI Reply" button
    ↓
Extension extracts the email content from the currently viewed email
    ↓
Extension sends email content to Spring Boot backend via HTTP POST
    ↓
Backend forwards request to Google Gemini API
    ↓
Gemini generates a professional reply
    ↓
Backend returns generated reply to Extension
    ↓
Extension inserts the reply into the Gmail compose box
    ↓
User can edit and send the reply
```

---

## Component Details

### **email-writer-ext** (Chrome Extension)

**Location:** `/SpringBoot/email-writer-ext/`

#### Files:
- **manifest.json**: Chrome extension configuration
  - Version: 3 (Manifest V3)
  - Permissions: Storage, Active Tab
  - Host Permissions: `http://localhost:8080/*` and `*://mail.google.com/*`
  - Content Script: `content.js` runs on Gmail

- **content.js**: Main extension logic
  - **getEmailContent()**: Extracts the email body from Gmail's DOM using multiple selectors
  - **findComposeToolbar()**: Locates Gmail's compose toolbar to inject the AI button
  - **createAIButton()**: Creates the styled "AI Reply" button matching Gmail's UI
  - **injectButton()**: 
    - Injects the button into the compose toolbar
    - Handles button click events
    - Makes POST request to backend API (`http://localhost:8080/api/gemini/generate`)
    - Inserts generated reply into the compose box
  - **MutationObserver**: Watches for new compose windows and automatically injects the button

- **content.css**: Styling for the extension (not shown in excerpt)

#### How It Works:
1. Loads when user visits Gmail
2. Watches for compose window creation using MutationObserver
3. When detected, injects the "AI Reply" button into Gmail's toolbar
4. On button click, it:
   - Extracts the current email content
   - Sends it to the backend with tone parameter
   - Receives AI-generated reply
   - Inserts reply into Gmail's compose box

---

### **email-writer-sb** (Spring Boot Backend)

**Location:** `/SpringBoot/email-writer-sb/`

#### Architecture:

**Configuration (GeminiClientConfig.java)**
- Initializes Google Gemini AI Client as a Spring Bean
- Reads API key from environment variable: `GEMINI_API_KEY`
- Uses builder pattern to configure the client

**Model (EmailRequest.java)**
```java
{
  "emailContent": "Original email text here",
  "tone": "professional" // or other tones
}
```

**Controller (EmailGeneratorController.java)**
- REST endpoint: `POST /api/gemini/generate`
- Accepts EmailRequest JSON body
- Calls EmailService to process the request
- Returns generated email reply as plain text
- CORS enabled for all origins

**Service (EmailService.java)**
- **generateEmailReply()**: 
  - Takes EmailRequest as input
  - Builds a prompt using buildPrompt()
  - Calls Gemini 2.5 Flash model via client
  - Returns generated text or error message
  
- **buildPrompt()**: 
  - Constructs prompt for Gemini AI
  - Instructions: "Generate professional email reply"
  - Incorporates tone parameter (e.g., "professional", "friendly", "formal")
  - Appends original email content
  - Excludes subject line from generation

#### Dependencies:
- **Spring Boot 4.0.0**: Web framework
- **Google Genai SDK**: Gemini API client
- **Lombok**: Reduces boilerplate code
- **Apache Commons Lang3**: Utility functions

#### Configuration:
- Server runs on: `http://localhost:8080`
- API Key: Read from `GEMINI_API_KEY` environment variable
- Java Version: 25

---

## Data Flow Diagram

```
┌─────────────────────────┐
│   Gmail Web Interface   │
│    (mail.google.com)    │
└────────────┬────────────┘
             │
             │ (Extract email content)
             │
         ┌───▼──────────────────────────┐
         │  Chrome Extension (content.js)│
         │  - Detects compose window    │
         │  - Injects AI Reply button   │
         │  - Extracts email text       │
         │  - Makes HTTP POST request   │
         └───┬──────────────────────────┘
             │
             │ HTTP POST
             │ {emailContent, tone}
             │
  ┌──────────▼──────────────────────────┐
  │   Spring Boot Backend                │
  │   (http://localhost:8080)            │
  │                                      │
  │  ┌─ EmailGeneratorController        │
  │  │   POST /api/gemini/generate      │
  │  │                                   │
  │  └─ EmailService                    │
  │     - buildPrompt()                  │
  │     - generateEmailReply()           │
  │                                      │
  │  ┌─ GeminiClientConfig              │
  │  │   (initializes Gemini client)    │
  │  └─────────────────────────────────│
  │                                      │
  └──────────┬───────────────────────────┘
             │
             │ (API Call)
             │
  ┌──────────▼───────────────────────────┐
  │   Google Gemini 2.5 Flash API        │
  │   - Generates professional reply     │
  │   - Returns generated text           │
  └──────────┬───────────────────────────┘
             │
             │ Generated reply text
             │
  ┌──────────▼──────────────────────────┐
  │  Spring Boot Backend                 │
  │  (Returns response.text())           │
  └──────────┬───────────────────────────┘
             │
             │ HTTP Response (plain text)
             │
  ┌──────────▼──────────────────────────┐
  │  Chrome Extension                    │
  │  - Receives generated reply          │
  │  - Inserts into compose box          │
  │  - Updates button state              │
  └──────────┬───────────────────────────┘
             │
             │ (Insert text)
             │
  ┌──────────▼──────────────────────────┐
  │   Gmail Compose Box                  │
  │   (User can edit and send)           │
  └──────────────────────────────────────┘
```

---

## Setup & Installation

### Prerequisites
- Chrome browser
- Java 25
- Maven
- Google Gemini API key

### Backend Setup (Spring Boot)

1. **Set API Key**
   ```bash
   set GEMINI_API_KEY=your_api_key_here  # Windows
   export GEMINI_API_KEY=your_api_key_here  # Linux/Mac
   ```

2. **Build & Run**
   ```bash
   cd SpringBoot/email-writer-sb
   mvn clean install
   mvn spring-boot:run
   ```
   Server will start on `http://localhost:8080`

3. **Test Endpoint**
   ```bash
   curl -X POST http://localhost:8080/api/gemini/generate \
     -H "Content-Type: application/json" \
     -d '{"emailContent":"Hello, how are you?","tone":"professional"}'
   ```

### Extension Setup

1. **Load in Chrome**
   - Open Chrome and go to: `chrome://extensions/`
   - Enable "Developer mode" (toggle in top right)
   - Click "Load unpacked"
   - Select the `SpringBoot/email-writer-ext` folder

2. **Verify Installation**
   - Go to Gmail (mail.google.com)
   - Open any email
   - Click compose/reply
   - Look for "AI Reply" button in the toolbar

---

## Usage

1. Open Gmail and select an email to reply to
2. Click the compose/reply button to open the compose box
3. The "AI Reply" button will appear in the toolbar (injected by extension)
4. Click "AI Reply"
5. Wait for the AI to generate a response (button shows "Generating a reply...")
6. The generated reply will be inserted into the compose box
7. Edit if needed and send

---

## Key Features

✅ **Seamless Gmail Integration** - Works directly within Gmail's web interface
✅ **AI-Powered Generation** - Uses Google Gemini 2.5 Flash for high-quality responses
✅ **Tone Customization** - Supports different email tones (professional, friendly, formal, etc.)
✅ **Real-time Updates** - Button shows loading state while generating
✅ **User Editable** - Generated reply can be edited before sending
✅ **CORS Enabled** - Backend allows requests from any origin
✅ **Error Handling** - Graceful error messages on API failures

---

## Technologies Used

### Frontend (Extension)
- JavaScript (ES6+)
- Chrome Extension API v3
- DOM Manipulation
- Fetch API
- MutationObserver

### Backend
- Java 25
- Spring Boot 4.0.0
- Spring WebFlux
- Maven
- Google Genai SDK
- Lombok

---

## API Reference

### Endpoint: Generate Email Reply

**Request:**
```
POST /api/gemini/generate
Host: localhost:8080
Content-Type: application/json

{
  "emailContent": "Original email body text here...",
  "tone": "professional"
}
```

**Response:**
```
200 OK
Content-Type: text/plain

Generated professional email reply text here...
```

**Error Response:**
```
500 Internal Server Error
Content-Type: text/plain

Error: API Request Failed.
```

---

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `GEMINI_API_KEY` | Google Gemini API key | `sk-...` |

---

## Future Enhancements

- [ ] Support for different email tones (friendly, formal, casual, etc.)
- [ ] Email template selection
- [ ] Reply history and favorites
- [ ] Customizable prompt templates
- [ ] Multi-language support
- [ ] Settings panel in extension
- [ ] Support for other email providers (Outlook, Apple Mail)
- [ ] Batch email reply generation
- [ ] Analytics and usage tracking

---

## Troubleshooting

### Extension not showing "AI Reply" button
- Ensure backend is running on `http://localhost:8080`
- Check browser console for errors (F12 → Console)
- Reload Gmail page (Ctrl+R)
- Disable and re-enable extension

### Backend API errors
- Verify `GEMINI_API_KEY` environment variable is set
- Check backend logs: `mvn spring-boot:run`
- Ensure port 8080 is not in use
- Try curl to test endpoint directly

### CORS errors
- Backend has `@CrossOrigin(origins = "*")` enabled
- Check browser Network tab (F12 → Network)
- Verify Content-Type header is set to `application/json`

---

### Attached Screenshots

1. Main Email Content
   <img width="1706" height="846" alt="image" src="https://github.com/user-attachments/assets/4f13785b-566c-4624-a8f4-d43e40b68faa" />

2. Click on reply button and a dialogue button will be appeared with AI-reply button
   <img width="1500" height="737" alt="image" src="https://github.com/user-attachments/assets/67dadafe-6528-43d6-b47d-4729122b88b7" />

3. Click on AI-Reply button, it will make a API call to backend to generate a reply
   <img width="1513" height="815" alt="image" src="https://github.com/user-attachments/assets/30a1d648-541d-4ffa-ada8-7c2be72ce7d8" />

4. Final Reply Content
   <img width="1550" height="747" alt="image" src="https://github.com/user-attachments/assets/1f338005-11d4-4194-81c1-02e190f7be6a" />


## License

This project is open source and available for personal and educational use.
