import React from 'react';

interface MarkdownRendererProps {
  content: string;
}

export const MarkdownRenderer: React.FC<MarkdownRendererProps> = ({ content }) => {
  if (!content) return null;

  // Split content into blocks: code blocks, tables, lists, and paragraphs
  const renderFormattedInline = (text: string) => {
    // Replace inline code `code`
    const parts = text.split(/(`[^`]+`|\*\*[^*]+\*\*|\*[^*]+\*)/g);
    return parts.map((part, idx) => {
      if (part.startsWith('`') && part.endsWith('`')) {
        return (
          <code
            key={idx}
            style={{
              background: '#27272a',
              color: '#38bdf8',
              padding: '2px 6px',
              borderRadius: '4px',
              fontSize: '12px',
              fontFamily: 'Consolas, monospace',
            }}
          >
            {part.slice(1, -1)}
          </code>
        );
      }
      if (part.startsWith('**') && part.endsWith('**')) {
        return <strong key={idx} style={{ color: '#ffffff', fontWeight: 600 }}>{part.slice(2, -2)}</strong>;
      }
      if (part.startsWith('*') && part.endsWith('*')) {
        return <em key={idx} style={{ color: '#e4e4e7' }}>{part.slice(1, -1)}</em>;
      }
      return part;
    });
  };

  const lines = content.replace(/\r\n/g, '\n').replace(/\r/g, '\n').split('\n');
  const blocks: React.ReactNode[] = [];
  let i = 0;

  while (i < lines.length) {
    const line = lines[i];

    // 1. Fenced Code Block
    if (line.trim().startsWith('```')) {
      const lang = line.trim().replace(/^```/, '');
      const codeLines: string[] = [];
      i++;
      while (i < lines.length && !lines[i].trim().startsWith('```')) {
        codeLines.push(lines[i]);
        i++;
      }
      i++; // skip closing ```
      blocks.push(
        <div
          key={`code_${i}`}
          style={{
            background: '#09090b',
            border: '1px solid #27272a',
            borderRadius: '8px',
            padding: '12px',
            margin: '8px 0',
            overflowX: 'auto',
          }}
        >
          {lang && (
            <div style={{ fontSize: '11px', color: '#71717a', marginBottom: '6px', textTransform: 'uppercase' }}>
              {lang}
            </div>
          )}
          <pre style={{ margin: 0, fontFamily: 'Consolas, monospace', fontSize: '13px', color: '#38bdf8' }}>
            {codeLines.join('\n')}
          </pre>
        </div>
      );
      continue;
    }

    // 2. Markdown Table Detection
    if (line.trim().startsWith('|') && line.trim().endsWith('|') && i + 1 < lines.length && lines[i + 1].includes('---')) {
      const headerCells = line.split('|').slice(1, -1).map((c) => c.trim());
      i += 2; // skip header and divider

      const rows: string[][] = [];
      while (i < lines.length && lines[i].trim().startsWith('|') && lines[i].trim().endsWith('|')) {
        const cells = lines[i].split('|').slice(1, -1).map((c) => c.trim());
        rows.push(cells);
        i++;
      }

      blocks.push(
        <div key={`table_${i}`} style={{ overflowX: 'auto', margin: '12px 0' }}>
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              fontSize: '13px',
              textAlign: 'left',
              border: '1px solid #27272a',
              borderRadius: '8px',
              overflow: 'hidden',
            }}
          >
            <thead>
              <tr style={{ background: '#1e1b4b', borderBottom: '2px solid #3730a3' }}>
                {headerCells.map((h, hIdx) => (
                  <th key={hIdx} style={{ padding: '8px 12px', color: '#c7d2fe', fontWeight: 600 }}>
                    {renderFormattedInline(h)}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row, rIdx) => (
                <tr
                  key={rIdx}
                  style={{
                    background: rIdx % 2 === 0 ? '#18181b' : '#121214',
                    borderBottom: '1px solid #27272a',
                  }}
                >
                  {row.map((cell, cIdx) => (
                    <td key={cIdx} style={{ padding: '8px 12px', color: '#e4e4e7' }}>
                      {renderFormattedInline(cell)}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
      continue;
    }

    // 3. Headers
    if (line.startsWith('### ')) {
      blocks.push(
        <h4 key={`h3_${i}`} style={{ margin: '12px 0 6px 0', fontSize: '15px', color: '#facc15', fontWeight: 600 }}>
          {renderFormattedInline(line.substring(4))}
        </h4>
      );
      i++;
      continue;
    }
    if (line.startsWith('## ')) {
      blocks.push(
        <h3 key={`h2_${i}`} style={{ margin: '14px 0 8px 0', fontSize: '16px', color: '#facc15', fontWeight: 600 }}>
          {renderFormattedInline(line.substring(3))}
        </h3>
      );
      i++;
      continue;
    }
    if (line.startsWith('# ')) {
      blocks.push(
        <h2 key={`h1_${i}`} style={{ margin: '16px 0 10px 0', fontSize: '18px', color: '#facc15', fontWeight: 700 }}>
          {renderFormattedInline(line.substring(2))}
        </h2>
      );
      i++;
      continue;
    }

    // 4. Bullet lists
    if (line.trim().startsWith('- ') || line.trim().startsWith('* ') || line.trim().startsWith('• ')) {
      const listItems: string[] = [];
      while (i < lines.length && (lines[i].trim().startsWith('- ') || lines[i].trim().startsWith('* ') || lines[i].trim().startsWith('• '))) {
        listItems.push(lines[i].trim().replace(/^[-*•]\s+/, ''));
        i++;
      }
      blocks.push(
        <ul key={`ul_${i}`} style={{ margin: '6px 0 10px 20px', padding: 0, color: '#e4e4e7', fontSize: '13px', lineHeight: 1.6 }}>
          {listItems.map((item, idx) => (
            <li key={idx} style={{ marginBottom: '4px' }}>
              {renderFormattedInline(item)}
            </li>
          ))}
        </ul>
      );
      continue;
    }

    // 5. Empty line
    if (line.trim() === '') {
      i++;
      continue;
    }

    // 6. Regular paragraph line
    blocks.push(
      <p key={`p_${i}`} style={{ margin: '4px 0 8px 0', lineHeight: 1.5, color: '#ffffff' }}>
        {renderFormattedInline(line)}
      </p>
    );
    i++;
  }

  return <div style={{ display: 'flex', flexDirection: 'column' }}>{blocks}</div>;
};
