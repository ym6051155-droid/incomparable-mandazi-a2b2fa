مكتبة SUST — نسخة V19 بأسماء تنزيل حسب المادة

هذه النسخة مبنية مباشرة على V19 وتحافظ على:
- موضع الصورة الخاص بالعربية.
- موضع الصورة الخاص بالإنجليزية.
- جرس التنبيهات وnotifications.json من GitHub.
- أيقونة Telegram.
- جميع وظائف الموقع الموجودة في V19.

التعديل الجديد:
عند تنزيل الملفات تتغير الأسماء تلقائيًا اعتمادًا على اسم المادة.

أمثلة:
- Heat-Transfer-II-Lecture-01.pdf
- Modeling-and-Simulation-Lecture-02.pdf
- Process-Control-I-Tutorial-01.pdf
- Mass-Transfer-Operations-I-Exam-01.pdf

لا تحتاج إلى إعادة تسمية الملفات داخل Cloudflare R2.
أسماء الملفات الأصلية مثل lecture-01.pdf يمكن أن تبقى كما هي.

طريقة النشر:
1. فك ضغط الحزمة.
2. استبدل ملفات V19 القديمة بهذه الملفات.
3. ارفعها إلى GitHub.
4. اضغط Commit changes.
5. انتظر نجاح Netlify Deploy.

مهم:
تغيير اسم الملف يعتمد على أن Cloudflare R2 يسمح لموقع:
https://sust-library.site
بقراءة الملفات عبر CORS.
عند منع CORS سيُفتح الملف بالاسم الأصلي كخطة بديلة.
