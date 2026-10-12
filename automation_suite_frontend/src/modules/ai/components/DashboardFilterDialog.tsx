import React, { useState, useEffect } from 'react';
import { X, Check } from 'lucide-react';

interface DashboardOption {
  id: string;
  label: string;
}

interface DashboardFilterDialogProps {
  open: boolean;
  onClose: () => void;
  selectedDashboards: string[];
  onApply: (dashboards: string[]) => void;
}

export const DASHBOARD_OPTIONS: DashboardOption[] = [
  { id: 'auto', label: 'Auto' },
  { id: 'terminal', label: 'Terminal' },
  { id: 'database', label: 'Database' },
];

export const DashboardFilterDialog: React.FC<DashboardFilterDialogProps> = ({
  open,
  onClose,
  selectedDashboards,
  onApply,
}) => {
  const [tempSelected, setTempSelected] = useState<string[]>(selectedDashboards);

  useEffect(() => {
    setTempSelected(selectedDashboards.length === 0 ? ['auto'] : selectedDashboards);
  }, [selectedDashboards, open]);

  if (!open) return null;

  const toggleOption = (id: string) => {
    if (id === 'auto') {
      setTempSelected(['auto']);
      return;
    }
    const currentWithoutAuto = tempSelected.filter((d) => d !== 'auto');
    if (currentWithoutAuto.includes(id)) {
      const updated = currentWithoutAuto.filter((d) => d !== id);
      setTempSelected(updated.length === 0 ? ['auto'] : updated);
    } else {
      setTempSelected([...currentWithoutAuto, id]);
    }
  };

  const handleApply = () => {
    onApply(tempSelected.length === 0 ? ['auto'] : tempSelected);
    onClose();
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
        backdropFilter: 'blur(4px)',
      }}
      onClick={onClose}
    >
      <div
        style={{
          width: '380px',
          backgroundColor: '#18181b',
          border: '1px solid #27272a',
          borderRadius: '12px',
          boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.5)',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Modal Header */}
        <div
          style={{
            padding: '16px 20px',
            borderBottom: '1px solid #27272a',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#121212',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>Select Target Dashboard</span>
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'transparent',
              border: 'none',
              color: '#a1a1aa',
              cursor: 'pointer',
              padding: '4px',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'center',
            }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Modal Body Options */}
        <div style={{ padding: '20px', display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {DASHBOARD_OPTIONS.map((opt) => {
            const isSelected = tempSelected.includes(opt.id);
            return (
              <div
                key={opt.id}
                onClick={() => toggleOption(opt.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '12px 16px',
                  backgroundColor: isSelected ? 'rgba(234, 179, 8, 0.1)' : '#09090b',
                  border: isSelected ? '1px solid #eab308' : '1px solid #27272a',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  transition: 'all 0.15s ease',
                }}
              >
                <div style={{ display: 'flex', flexDirection: 'column' }}>
                  <span style={{ fontSize: '13px', fontWeight: 600, color: isSelected ? '#eab308' : '#ffffff' }}>
                    {opt.label}
                  </span>
                </div>
                {isSelected && <Check size={16} color="#eab308" />}
              </div>
            );
          })}
        </div>

        {/* Modal Footer Actions */}
        <div
          style={{
            padding: '12px 20px',
            borderTop: '1px solid #27272a',
            display: 'flex',
            justifyContent: 'flex-end',
            gap: '8px',
            backgroundColor: '#121212',
          }}
        >
          <button
            onClick={onClose}
            style={{
              padding: '6px 14px',
              borderRadius: '6px',
              border: '1px solid #27272a',
              background: '#18181b',
              color: '#a1a1aa',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Cancel
          </button>
          <button
            onClick={handleApply}
            style={{
              padding: '6px 16px',
              borderRadius: '6px',
              border: 'none',
              background: '#eab308',
              color: '#000000',
              fontSize: '12px',
              fontWeight: 700,
              cursor: 'pointer',
            }}
          >
            Apply Selection
          </button>
        </div>
      </div>
    </div>
  );
};
