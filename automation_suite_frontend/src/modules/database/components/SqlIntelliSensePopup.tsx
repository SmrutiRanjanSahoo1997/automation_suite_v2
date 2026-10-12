import React, { useEffect } from 'react';

interface SqlIntelliSensePopupProps {
  suggestions: { text: string; type: string }[];
  selectedIndex: number;
  position: { top: number; left: number };
  onSelect: (item: { text: string; type: string }) => void;
}

export const SqlIntelliSensePopup: React.FC<SqlIntelliSensePopupProps> = ({
  suggestions,
  selectedIndex,
  position,
  onSelect,
}) => {
  if (suggestions.length === 0) return null;

  return (
    <div
      className="db-intellisense-popup"
      style={{
        top: Math.min(position.top, 200),
        left: Math.min(position.left, 500),
      }}
    >
      {suggestions.map((item, idx) => (
        <div
          key={`${item.text}-${idx}`}
          className={`db-intellisense-item ${idx === selectedIndex ? 'selected' : ''}`}
          onClick={() => onSelect(item)}
          onMouseDown={(e) => e.preventDefault()}
        >
          <span>{item.text}</span>
          <span className="db-intellisense-tag">{item.type}</span>
        </div>
      ))}
    </div>
  );
};
