import React, { useState, useMemo } from 'react';
import { ServerConfig, Environment, ServerCategory } from '../types/terminal.types';
import { Search, ChevronDown, ChevronRight, X, Check } from 'lucide-react';

interface Props {
  open: boolean;
  onClose: () => void;
  servers: ServerConfig[];
  selectedHosts: string[];
  onApply: (selectedHosts: string[]) => void;
}

const ENV_ORDER: Array<{ code: Environment; label: string }> = [
  { code: 'PROD', label: 'PRODUCTION' },
  { code: 'T01', label: 'T01' },
  { code: 'T02', label: 'T02' },
  { code: 'T03', label: 'T03' },
  { code: 'T04', label: 'T04' },
  { code: 'DEV', label: 'DEVELOPMENT' },
];

const SERVER_TYPE_ORDER: Array<{ code: ServerCategory; label: string }> = [
  { code: 'INSPIRE_AUTOMATION', label: 'Inspire Automation' },
  { code: 'INSPIRE_SCALER', label: 'Inspire Scaler' },
  { code: 'IPS_POOL', label: 'IPS Pool' },
  { code: 'INTERACTIVE', label: 'Interactive' },
];

export const ServerFilterDialog: React.FC<Props> = ({
  open,
  onClose,
  servers,
  selectedHosts,
  onApply,
}) => {
  const [search, setSearch] = useState('');
  const [localSelected, setLocalSelected] = useState<Set<string>>(() => new Set(selectedHosts));
  const [expandedEnvs, setExpandedEnvs] = useState<Set<string>>(() => new Set(['PROD', 'DEV', 'T01']));
  const [expandedTypes, setExpandedTypes] = useState<Set<string>>(new Set());

  if (!open) return null;

  const totalUniqueHosts = servers.length;

  const toggleHost = (host: string) => {
    setLocalSelected((prev) => {
      const next = new Set(prev);
      if (next.has(host)) next.delete(host);
      else next.add(host);
      return next;
    });
  };

  const toggleEnv = (envCode: string) => {
    const envHosts = servers.filter((s) => (s.env || s.environment) === envCode).map((s) => s.host);
    const allSelected = envHosts.every((h) => localSelected.has(h));
    setLocalSelected((prev) => {
      const next = new Set(prev);
      envHosts.forEach((h) => {
        if (allSelected) next.delete(h);
        else next.add(h);
      });
      return next;
    });
  };

  const toggleType = (envCode: string, serverType: ServerCategory) => {
    const typeHosts = servers
      .filter((s) => (s.env || s.environment) === envCode && (s.serverType || s.category) === serverType)
      .map((s) => s.host);
    const allSelected = typeHosts.every((h) => localSelected.has(h));
    setLocalSelected((prev) => {
      const next = new Set(prev);
      typeHosts.forEach((h) => {
        if (allSelected) next.delete(h);
        else next.add(h);
      });
      return next;
    });
  };

  const toggleExpandEnv = (envCode: string) => {
    setExpandedEnvs((prev) => {
      const next = new Set(prev);
      if (next.has(envCode)) next.delete(envCode);
      else next.add(envCode);
      return next;
    });
  };

  const toggleExpandType = (key: string) => {
    setExpandedTypes((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key);
      else next.add(key);
      return next;
    });
  };

  const handleSelectAll = () => {
    setLocalSelected(new Set(servers.map((s) => s.host)));
  };

  const handleDeselectAll = () => {
    setLocalSelected(new Set());
  };

  const handleSave = () => {
    onApply(Array.from(localSelected));
    onClose();
  };

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="rename-dialog" style={{ width: '540px', maxHeight: '85vh', display: 'flex', flexDirection: 'column' }} onClick={(e) => e.stopPropagation()}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '12px', borderBottom: '1px solid #334155' }}>
          <div style={{ fontSize: '18px', fontWeight: 700 }}>Server Selection Filter</div>
          <span className="badge-connected" style={{ background: '#3b82f6', color: '#ffffff', border: 'none' }}>
            {localSelected.size}/{totalUniqueHosts} Selected
          </span>
        </div>

        <div style={{ display: 'flex', gap: '8px', margin: '14px 0' }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <Search size={16} style={{ position: 'absolute', left: '10px', top: '10px', color: '#94a3b8' }} />
            <input
              type="text"
              className="filter-select"
              style={{ width: '100%', paddingLeft: '32px', boxSizing: 'border-box' }}
              placeholder="Search hostname (H1191), environment..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <button onClick={handleSelectAll} style={{ background: '#334155', color: '#f8fafc', border: '1px solid #475569', borderRadius: '4px', padding: '0 12px', cursor: 'pointer' }}>All</button>
          <button onClick={handleDeselectAll} style={{ background: '#334155', color: '#94a3b8', border: '1px solid #475569', borderRadius: '4px', padding: '0 12px', cursor: 'pointer' }}>None</button>
        </div>

        <div style={{ flex: 1, overflowY: 'auto', paddingRight: '4px' }}>
          {ENV_ORDER.map(({ code: envCode, label: envLabel }) => {
            const rawEnvItems = servers.filter((s) => (s.env || s.environment) === envCode);
            const totalEnvHosts = Array.from(new Set(rawEnvItems.map((s) => s.host)));
            if (totalEnvHosts.length === 0) return null;

            const selectedInEnv = totalEnvHosts.filter((h) => localSelected.has(h)).length;
            const isExpanded = expandedEnvs.has(envCode);

            return (
              <div key={envCode} style={{ marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '6px', background: 'rgba(255,255,255,0.03)', borderRadius: '4px' }}>
                  <span onClick={() => toggleExpandEnv(envCode)} style={{ cursor: 'pointer', color: '#94a3b8' }}>
                    {isExpanded ? <ChevronDown size={18} /> : <ChevronRight size={18} />}
                  </span>
                  <input
                    type="checkbox"
                    checked={selectedInEnv === totalEnvHosts.length}
                    onChange={() => toggleEnv(envCode)}
                  />
                  <span style={{ fontWeight: 700, fontSize: '14px', flex: 1 }}>{envLabel}</span>
                  <span style={{ fontSize: '11px', color: '#94a3b8', background: 'rgba(255,255,255,0.08)', padding: '2px 6px', borderRadius: '10px' }}>
                    {selectedInEnv}/{totalEnvHosts.length}
                  </span>
                </div>

                {isExpanded && (
                  <div style={{ paddingLeft: '24px', marginTop: '4px' }}>
                    {SERVER_TYPE_ORDER.map(({ code: typeCode, label: typeLabel }) => {
                      const typeHosts = rawEnvItems.filter((s) => (s.serverType || s.category) === typeCode).map((s) => s.host);
                      if (typeHosts.length === 0) return null;

                      const selectedInType = typeHosts.filter((h) => localSelected.has(h)).length;
                      const typeKey = `${envCode}-${typeCode}`;
                      const isTypeExpanded = expandedTypes.has(typeKey);

                      return (
                        <div key={typeKey} style={{ marginBottom: '4px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '4px' }}>
                            <span onClick={() => toggleExpandType(typeKey)} style={{ cursor: 'pointer', color: '#94a3b8' }}>
                              {isTypeExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                            </span>
                            <input
                              type="checkbox"
                              checked={selectedInType === typeHosts.length}
                              onChange={() => toggleType(envCode, typeCode)}
                            />
                            <span style={{ fontSize: '13px', color: '#cbd5e1', flex: 1 }}>{typeLabel}</span>
                            <span style={{ fontSize: '11px', color: '#94a3b8' }}>{selectedInType}/{typeHosts.length}</span>
                          </div>

                          {isTypeExpanded && (
                            <div style={{ paddingLeft: '28px' }}>
                              {typeHosts.map((host) => (
                                <label key={host} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '2px 0', fontSize: '13px', color: localSelected.has(host) ? '#f8fafc' : '#94a3b8', cursor: 'pointer' }}>
                                  <input
                                    type="checkbox"
                                    checked={localSelected.has(host)}
                                    onChange={() => toggleHost(host)}
                                  />
                                  <span>{host}</span>
                                </label>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })}
        </div>

        <div className="dialog-actions" style={{ marginTop: '16px', paddingTop: '12px', borderTop: '1px solid #334155' }}>
          <button onClick={onClose} style={{ background: '#334155', color: '#f8fafc', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer' }}>Cancel</button>
          <button onClick={handleSave} style={{ background: '#3b82f6', color: '#ffffff', border: 'none', padding: '8px 16px', borderRadius: '4px', cursor: 'pointer', fontWeight: 600 }}>Apply Selection</button>
        </div>
      </div>
    </div>
  );
};
