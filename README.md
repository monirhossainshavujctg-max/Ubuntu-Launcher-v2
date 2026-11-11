# Ubuntu Launcher (Android)

হালকা, দ্রুত, Ubuntu-স্টাইল লঞ্চার। কোনো ছবি/ফন্ট/থার্ড-পার্টি লাইব্রেরি ছাড়া (শুধু AndroidX)।

## ফিচার
- বাম পাশে Ubuntu ডক (লং-প্রেস করে ডকে যোগ/সরানো, App info, Uninstall)
- "Show Applications" গ্রিড + সার্চ (Enter চাপলে প্রথম রেজাল্ট খোলে)
- উপরে টপ বার: Activities, তারিখ-সময়, ব্যাটারি, নোটিফিকেশন সংখ্যা
- নোটিফিকেশন শেড: উপর থেকে নিচে সোয়াইপ বা টপ বারে ট্যাপ; কার্ড-স্টাইল, সোয়াইপ করে সরানো, Clear all, কুইক বাটন
- নিচ থেকে উপরে সোয়াইপ = অ্যাপ ড্রয়ার
- Ubuntu aubergine → orange ওয়ালপেপার

## APK বানানো
### ১) Android Studio
File → Open → এই ফোল্ডার → Sync শেষ হলে Build → Build APK(s)
(Release APK: Build → Generate Signed App Bundle/APK, অথবা টার্মিনালে `gradle assembleRelease`)

### ২) GitHub Actions (কিছু ইনস্টল না করেই)
এই ফোল্ডারটা একটা GitHub রিপোজিটরিতে আপলোড করুন → Actions ট্যাব → "Build APK" → শেষে Artifacts থেকে APK ডাউনলোড।

## চালু করা
1. APK ইনস্টল করুন → Home চাপুন → "Ubuntu Launcher" বেছে "Always" দিন
2. শেডে "Allow notification access" চাপুন → Ubuntu Launcher চালু করুন
   (Android 13+ এ সাইডলোড অ্যাপের জন্য: Settings → Apps → Ubuntu Launcher → ⋮ → "Allow restricted settings", তারপর আবার চেষ্টা করুন)
