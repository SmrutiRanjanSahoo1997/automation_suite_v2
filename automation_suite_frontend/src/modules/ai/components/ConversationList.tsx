import React, { useState } from 'react';
import { Bot, Plus, Trash2, Edit3, Check, X } from 'lucide-react';
import { AiConversationRecord } from '../repository/aiConversationRepo';

interface ConversationListProps {
  conversations: AiConversationRecord[];
  activeConvId: string | null;
  onSelectConversation: (conv: AiConversationRecord) => void;
  onNewChat: () => void;
  onRenameConversation: (id: string, newTitle: string) => void;
  onDeleteConversation: (id: string) => void;
}

export const ConversationList: React.FC<ConversationListProps> = ({
  conversations,
  activeConvId,
  onSelectConversation,
  onNewChat,
  onRenameConversation,
  onDeleteConversation,
}) => {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editTitleText, setEditTitleText] = useState('');

  const handleStartEdit = (e: React.MouseEvent, conv: AiConversationRecord) => {
    e.stopPropagation();
    setEditingId(conv.id);
    setEditTitleText(conv.title);
  };

  const handleSaveEdit = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    if (editTitleText.trim()) {
      onRenameConversation(id, editTitleText.trim());
    }
    setEditingId(null);
  };

  const handleDelete = (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    onDeleteConversation(id);
  };

  return (
    <aside style={{ width: '300px', background: '#121212', borderLeft: '1px solid #27272a', display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Drawer Header */}
      <div style={{ padding: '16px', borderBottom: '1px solid #27272a', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div style={{ fontWeight: 700, fontSize: '14px', color: '#ffffff', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Bot style={{ width: '18px', height: '18px', color: '#eab308' }} />
          <span>AI Conversations</span>
        </div>

        <button
          onClick={onNewChat}
          style={{
            background: '#eab308',
            color: '#000000',
            border: 'none',
            borderRadius: '6px',
            padding: '6px 10px',
            fontSize: '12px',
            fontWeight: 700,
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
          }}
        >
          <Plus style={{ width: '14px', height: '14px' }} />
          New
        </button>
      </div>

      {/* Conversation List */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '12px 8px', display: 'flex', flexDirection: 'column', gap: '4px' }}>
        {conversations.length === 0 ? (
          <div style={{ padding: '16px', fontSize: '12px', color: '#71717a', textAlign: 'center' }}>
            No saved conversations.
          </div>
        ) : (
          conversations.map((conv) => {
            const isActive = activeConvId === conv.id;
            const isEditing = editingId === conv.id;

            return (
              <div
                key={conv.id}
                onClick={() => onSelectConversation(conv)}
                style={{
                  padding: '10px 12px',
                  borderRadius: '6px',
                  background: isActive ? '#27272a' : 'transparent',
                  borderLeft: isActive ? '3px solid #eab308' : '3px solid transparent',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  transition: 'background 0.15s ease',
                }}
              >
                {isEditing ? (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', width: '100%' }} onClick={(e) => e.stopPropagation()}>
                    <input
                      type="text"
                      value={editTitleText}
                      onChange={(e) => setEditTitleText(e.target.value)}
                      style={{ flex: 1, background: '#0a0a0a', border: '1px solid #eab308', borderRadius: '4px', color: '#ffffff', padding: '4px 8px', fontSize: '12px' }}
                    />
                    <button onClick={(e) => handleSaveEdit(e, conv.id)} style={{ background: 'none', border: 'none', color: '#22c55e', cursor: 'pointer', padding: '2px' }}>
                      <Check style={{ width: '14px', height: '14px' }} />
                    </button>
                    <button onClick={(e) => { e.stopPropagation(); setEditingId(null); }} style={{ background: 'none', border: 'none', color: '#71717a', cursor: 'pointer', padding: '2px' }}>
                      <X style={{ width: '14px', height: '14px' }} />
                    </button>
                  </div>
                ) : (
                  <>
                    <span style={{ fontSize: '13px', color: isActive ? '#ffffff' : '#a1a1aa', fontWeight: isActive ? 600 : 400, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '200px' }}>
                      {conv.title}
                    </span>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <button onClick={(e) => handleStartEdit(e, conv)} style={{ background: 'none', border: 'none', color: '#71717a', cursor: 'pointer', padding: '2px' }}>
                        <Edit3 style={{ width: '13px', height: '13px' }} />
                      </button>
                      <button onClick={(e) => handleDelete(e, conv.id)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', padding: '2px' }}>
                        <Trash2 style={{ width: '13px', height: '13px' }} />
                      </button>
                    </div>
                  </>
                )}
              </div>
            );
          })
        )}
      </div>
    </aside>
  );
};
