import React, { useState, useRef, useEffect } from 'react';
import { TerminalSession } from '../types/terminal.types';
import { TerminalTabHeader } from './TerminalTabHeader';
import { TerminalConsoleCanvas } from './TerminalConsoleCanvas';

interface Props {
  sessions: TerminalSession[];
  activeTerminalId: string | null;
  onSelectTab: (terminalId: string) => void;
  onCloseTab: (terminalId: string) => void;
  onExecute: (terminalId: string, host: string, command: string) => void;
  onStop: (terminalId: string, host: string) => void;
  onCloseWindow: () => void;
  onPopOut?: (terminalId: string) => void;
}

export const TerminalModalWindow: React.FC<Props> = ({
  sessions,
  activeTerminalId,
  onSelectTab,
  onCloseTab,
  onExecute,
  onStop,
  onCloseWindow,
  onPopOut,
}) => {
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [isMinimized, setIsMinimized] = useState(false);

  // Window position & size state for free mouse resizing and dragging
  const [pos, setPos] = useState({ x: 120, y: 80 });
  const [size, setSize] = useState({ w: 850, h: 540 });
  const isDraggingRef = useRef(false);
  const isResizingRef = useRef(false);
  const resizeEdgeRef = useRef<string | null>(null);
  const dragStartRef = useRef({ mouseX: 0, mouseY: 0, winX: 0, winY: 0, winW: 0, winH: 0 });

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isDraggingRef.current) {
        const dx = e.clientX - dragStartRef.current.mouseX;
        const dy = e.clientY - dragStartRef.current.mouseY;
        setPos({
          x: Math.max(0, dragStartRef.current.winX + dx),
          y: Math.max(0, dragStartRef.current.winY + dy),
        });
      } else if (isResizingRef.current && resizeEdgeRef.current) {
        const dx = e.clientX - dragStartRef.current.mouseX;
        const dy = e.clientY - dragStartRef.current.mouseY;
        const edge = resizeEdgeRef.current;

        let newW = dragStartRef.current.winW;
        let newH = dragStartRef.current.winH;
        let newX = dragStartRef.current.winX;
        let newY = dragStartRef.current.winY;

        if (edge.includes('e')) newW = Math.max(400, dragStartRef.current.winW + dx);
        if (edge.includes('s')) newH = Math.max(250, dragStartRef.current.winH + dy);
        if (edge.includes('w')) {
          const possibleW = dragStartRef.current.winW - dx;
          if (possibleW > 400) {
            newW = possibleW;
            newX = dragStartRef.current.winX + dx;
          }
        }
        if (edge.includes('n')) {
          const possibleH = dragStartRef.current.winH - dy;
          if (possibleH > 250) {
            newH = possibleH;
            newY = dragStartRef.current.winY + dy;
          }
        }

        setSize({ w: newW, h: newH });
        setPos({ x: newX, y: newY });
      }
    };

    const handleMouseUp = () => {
      isDraggingRef.current = false;
      isResizingRef.current = false;
      resizeEdgeRef.current = null;
    };

    window.addEventListener('mousemove', handleMouseMove);
    window.addEventListener('mouseup', handleMouseUp);
    return () => {
      window.removeEventListener('mousemove', handleMouseMove);
      window.removeEventListener('mouseup', handleMouseUp);
    };
  }, []);

  const isAiSession = (s: TerminalSession) =>
    Boolean(s.isAiSession) ||
    Boolean(s.name && (s.name.toUpperCase().includes('ASR_AI') || s.name.toUpperCase().includes('ASR'))) ||
    Boolean(s.terminalId && (s.terminalId.toUpperCase().includes('ASR_AI') || s.terminalId.toUpperCase().includes('ASR')));

  const openSessions = sessions.filter((s) => s.state !== 'CLOSED' && !isAiSession(s));
  if (openSessions.length === 0) return null;

  const activeSession = openSessions.find((s) => s.terminalId === activeTerminalId) || openSessions[0];

  const startDrag = (e: React.MouseEvent) => {
    if (isFullscreen) return;
    isDraggingRef.current = true;
    dragStartRef.current = {
      mouseX: e.clientX,
      mouseY: e.clientY,
      winX: pos.x,
      winY: pos.y,
      winW: size.w,
      winH: size.h,
    };
  };

  const startResize = (e: React.MouseEvent, edge: string) => {
    e.stopPropagation();
    e.preventDefault();
    if (isFullscreen) return;
    isResizingRef.current = true;
    resizeEdgeRef.current = edge;
    dragStartRef.current = {
      mouseX: e.clientX,
      mouseY: e.clientY,
      winX: pos.x,
      winY: pos.y,
      winW: size.w,
      winH: size.h,
    };
  };

  return (
    <div className="terminal-modal-overlay">
      <div
        className={`terminal-modal-window ${isFullscreen ? 'fullscreen' : ''}`}
        style={
          isFullscreen
            ? {}
            : {
                top: isMinimized ? 'calc(100vh - 40px)' : `${pos.y}px`,
                left: `${pos.x}px`,
                width: `${size.w}px`,
                height: isMinimized ? '40px' : `${size.h}px`,
              }
        }
      >
        {/* Header Bar with Mouse Drag Move Listener */}
        <div onMouseDown={startDrag} style={{ cursor: isFullscreen ? 'default' : 'move' }}>
          <TerminalTabHeader
            sessions={openSessions}
            activeTerminalId={activeSession?.terminalId || null}
            onSelectTab={(id) => {
              setIsMinimized(false);
              onSelectTab(id);
            }}
            onCloseTab={onCloseTab}
            onToggleMinimize={() => setIsMinimized(!isMinimized)}
            onToggleFullscreen={() => setIsFullscreen(!isFullscreen)}
            onCloseWindow={onCloseWindow}
            onPopOut={onPopOut || ((tid) => window.open(`?terminalId=${encodeURIComponent(tid)}`, '_blank'))}
            isFullscreen={isFullscreen}
          />
        </div>

        {!isMinimized && activeSession && (
          <TerminalConsoleCanvas
            session={activeSession}
            onExecute={onExecute}
            onStop={onStop}
          />
        )}

        {/* Freely Resizable Edge Handles */}
        {!isFullscreen && !isMinimized && (
          <>
            <div onMouseDown={(e) => startResize(e, 'n')} style={{ position: 'absolute', top: 0, left: 0, right: 0, height: '6px', cursor: 'ns-resize' }} />
            <div onMouseDown={(e) => startResize(e, 's')} style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '6px', cursor: 'ns-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'w')} style={{ position: 'absolute', top: 0, bottom: 0, left: 0, width: '6px', cursor: 'ew-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'e')} style={{ position: 'absolute', top: 0, bottom: 0, right: 0, width: '6px', cursor: 'ew-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'nw')} style={{ position: 'absolute', top: 0, left: 0, width: '10px', height: '10px', cursor: 'nwse-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'ne')} style={{ position: 'absolute', top: 0, right: 0, width: '10px', height: '10px', cursor: 'nesw-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'sw')} style={{ position: 'absolute', bottom: 0, left: 0, width: '10px', height: '10px', cursor: 'nesw-resize' }} />
            <div onMouseDown={(e) => startResize(e, 'se')} style={{ position: 'absolute', bottom: 0, right: 0, width: '14px', height: '14px', cursor: 'nwse-resize', background: 'transparent' }} />
          </>
        )}
      </div>
    </div>
  );
};
