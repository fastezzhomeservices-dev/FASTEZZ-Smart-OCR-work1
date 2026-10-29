# FASTEZZ Smart OCR — Android Starter Project

यह Android Studio में खोलने के लिए शुरुआती source project है। यह अभी compiled APK नहीं है।

## अभी शामिल सुविधाएँ
- Camera से फोटो लेकर OCR (camera intent द्वारा)
- Gallery / screenshot से image चुनना
- On-device ML Kit OCR: Latin/common text और Devanagari text recognizers
- Editable OCR result
- Copy All, Share और device पर TXT file save करना

## Windows PC पर APK बनाने के चरण
1. Android Studio को आधिकारिक वेबसाइट से install करें: https://developer.android.com/studio
2. ZIP को extract करें।
3. Android Studio खोलें → **Open** → extracted `FASTEZZ_Smart_OCR_Android_Project` folder चुनें।
4. Gradle sync पूरा होने दें। पहली बार इंटरनेट की जरूरत dependencies डाउनलोड करने के लिए होगी।
5. अगर Android SDK 35 install करने को कहा जाए, SDK Manager से install करें।
6. फोन को USB से जोड़कर Developer options → USB debugging चालू करें, या Android Studio Emulator चलाएँ।
7. पहले **Run** बटन से app test करें।
8. APK बनाने के लिए **Build → Build APK(s)** चुनें। APK आम तौर पर `app/build/outputs/apk/debug/app-debug.apk` में बनेगी।

## महत्वपूर्ण सीमाएँ
- इस environment में Android SDK/Gradle installed नहीं थे, इसलिए APK यहाँ compile नहीं की गई।
- यह पहला starter version है; History screen, structured project-type forms, online AI, production privacy review और final branding अभी जोड़े जाने बाकी हैं।
- OCR के नतीजे image quality और script पर निर्भर हैं। जरूरी नाम, ID और numbers को हमेशा manually verify करें।
- प्रस्ताव में offline OCR और optional online AI है; इस starter build में online AI शामिल नहीं है।
- Camera action कुछ devices पर preview/thumbnail लौटाती है; बेहतर full-resolution camera capture अगले चरण में जोड़ा जा सकता है।


## GitHub से APK build करने का आसान तरीका
1. GitHub पर sign in करें और `New repository` चुनें।
2. Repository का नाम `FASTEZZ-Smart-OCR` रखें। Public या Private चुन सकते हैं; Create repository दबाएँ।
3. ZIP को पहले अपने कंप्यूटर पर Extract करें। Extract किए गए फ़ोल्डर के अंदर की सभी फाइलें और `.github` फ़ोल्डर repository में upload करें (ZIP को सीधे repository में डालना पर्याप्त नहीं है)।
4. `Commit changes` करें।
5. Repository में `Actions` tab खोलें। `Build FASTEZZ Smart OCR APK` workflow चुनें और `Run workflow` दबाएँ।
6. Build पूरा होने पर workflow run खोलें और नीचे `Artifacts` में `FASTEZZ-Smart-OCR-debug-APK` डाउनलोड करें। ZIP artifact के अंदर `app-debug.apk` होगा.

Build failure होने पर Actions page पर लाल failed run खोलकर error का screenshot या पूरा error text साझा करें।
