import React from 'react';
import { Plus, X } from 'lucide-react';
import { MssqlTab } from '../types';

interface WindowTabBarProps {
  tabs: MssqlTab[];
  activeTabId: string;
  onSelectTab: (tabId: string) => void;
  onAddTab: () => void;
  onCloseTab: (tabId: string) => void;
}

export const WindowTabBar: React.FC<WindowTabBarProps> = ({
  tabs,
  activeTabId,
  onSelectTab,
  onAddTab,
  onCloseTab,
}) => {
  return (
    <div className="db-tabs-bar">
      {tabs.map((tab) => {
        const isActive = tab.id === activeTabId;
        return (
          <div
            key={tab.id}
            className={`db-tab-item ${isActive ? 'active' : ''}`}
            onClick={() => onSelectTab(tab.id)}
          >
            <span>{tab.title}</span>
            {tabs.length > 1 && (
              <button
                className="db-tab-close-btn"
                onClick={(e) => {
                  e.stopPropagation();
                  onCloseTab(tab.id);
                }}
                title="Close Window"
              >
                <X size={13} />
              </button>
            )}
          </div>
        );
      })}
      <button className="db-tab-add-btn" onClick={onAddTab} title="New Window">
        <Plus size={14} />
        <span>New Window</span>
      </button>
    </div>
  );
};
