const CATEGORY_LABELS = {
  lectures: ['المحاضرة', 'Lecture'],
  tutorials: ['التوتريال', 'Tutorial'],
  assessments: ['المتابعة', 'Assessment'],
  explanations: ['الشرح', 'Explanation'],
  references: ['المرجع', 'Reference'],
  exams: ['الامتحان', 'Exam']
};

function labelFromFile(category, filename) {
  const stem = filename.replace(/\.[^.]+$/, '');
  const numbers = stem.match(/\d+(?:-\d+)?/);
  const number = numbers ? numbers[0].replace('-', '–') : '';
  const labels = CATEGORY_LABELS[category] || ['الملف', 'File'];
  return {
    number,
    labelAr: number ? `${labels[0]} ${number}` : filename,
    labelEn: number ? `${labels[1]} ${number}` : filename
  };
}

function parseObject(object) {
  const parts = object.key.split('/').filter(Boolean);
  if (parts.length < 5) return null;
  const [program, semester, course, category, ...fileParts] = parts;
  if (!CATEGORY_LABELS[category]) return null;
  const file = fileParts.join('/');
  const label = labelFromFile(category, file);
  return {
    courseRoot: `${program}/${semester}/${course}/`,
    category,
    number: label.number,
    file,
    labelAr: label.labelAr,
    labelEn: label.labelEn,
    mime: object.httpMetadata?.contentType || 'application/octet-stream',
    size: object.size,
    addedAt: object.uploaded ? object.uploaded.toISOString().slice(0, 10) : null,
    url: `/${object.key}`
  };
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (url.pathname !== '/api/library') {
      return new Response('Not found', { status: 404 });
    }

    const files = [];
    let cursor;
    do {
      const page = await env.SUST_LIBRARY.list({
        limit: 1000,
        cursor,
        include: ['httpMetadata', 'customMetadata']
      });
      for (const object of page.objects) {
        const parsed = parseObject(object);
        if (parsed) files.push(parsed);
      }
      cursor = page.truncated ? page.cursor : undefined;
    } while (cursor);

    files.sort((a, b) =>
      String(b.addedAt || '').localeCompare(String(a.addedAt || '')) ||
      a.courseRoot.localeCompare(b.courseRoot) ||
      a.category.localeCompare(b.category) ||
      a.file.localeCompare(b.file)
    );

    return Response.json(
      {
        version: 1,
        updatedAt: new Date().toISOString().slice(0, 10),
        source: 'cloudflare-r2',
        files,
        courseMeta: {}
      },
      {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Cache-Control': 'public, max-age=60'
        }
      }
    );
  }
};
