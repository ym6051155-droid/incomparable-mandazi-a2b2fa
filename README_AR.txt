مكتبة SUST — تنبيهات R2 — النسخة V16

التغيير:
- الموقع يقرأ التنبيهات من Cloudflare R2 مباشرة.
- رابط التنبيهات:
  https://pub-c9e3df8f82464009bba375c7b8a18044.r2.dev/notifications.json
- لم يعد رفع تنبيه جديد يحتاج GitHub أو Netlify Deploy.

أولًا: نشر الموقع مرة واحدة
1. ارفع ملفات هذه الحزمة إلى مستودع GitHub المرتبط بـNetlify.
2. استبدل الملفات القديمة.
3. اضغط Commit changes.
4. بعد نجاح النشر افتح:
   https://sust-library.site/?v=16

ثانيًا: إعداد CORS في R2 مرة واحدة
1. Cloudflare > R2 Object Storage.
2. افتح sust-elibrary.
3. Settings > CORS Policy > Add CORS policy.
4. انسخ محتوى r2-cors-policy.json ثم Save.

ثالثًا: رفع التنبيه إلى R2
1. افتح sust-elibrary.
2. ارفع notifications.json إلى جذر الحاوية.
3. عند وجود نسخة قديمة، استبدلها.
4. لا ترفعه داخل مجلد المحاضرات.

بعد ذلك، لإرسال أي تنبيه جديد:
1. افتح:
   https://sust-library.site/admin-notifications.html
2. أنشئ الرسالة ونزّل notifications.json.
3. استبدل الملف في R2 فقط.
