import { useState } from 'react';
import StatusBadge from './StatusBadge';

// Generic "list + assign + toggle" block used on Role/Group/Subject detail
// pages for the assignment-backed associations (role-permissions,
// group-permissions, role-groups, subject-roles). Each item is expected to
// carry a `status` and an `assignmentId`; items without an assignmentId
// (e.g. from an older list endpoint) render without a toggle control.
export default function AssociationSection({
  title,
  items,
  renderLabel,
  optionsList,
  selectLabel,
  onAssign,
  onToggle,
}) {
  const [selected, setSelected] = useState('');
  const [busy, setBusy] = useState(false);
  const [togglingId, setTogglingId] = useState(null);
  const [localError, setLocalError] = useState('');

  async function handleAssign(e) {
    e.preventDefault();
    if (!selected) return;
    setBusy(true);
    setLocalError('');
    try {
      await onAssign(selected);
      setSelected('');
    } catch (err) {
      setLocalError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function handleToggle(item) {
    setLocalError('');
    setTogglingId(item.assignmentId || item.toggleId || item.id);
    try {
      await onToggle(item);
    } catch (err) {
      setLocalError(err.message);
    } finally {
      setTogglingId(null);
    }
  }

  return (
    <div className="association-section">
      <h3>{title}</h3>
      {localError && <div className="error-banner">{localError}</div>}

      {items.length === 0 ? (
        <p className="muted">None</p>
      ) : (
        <ul>
          {items.map((item) => {
            const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
            const toggleId = item.assignmentId || item.toggleId || item.id;
            return (
              <li key={toggleId || item.id}>
                {renderLabel(item)} <StatusBadge status={item.status} />{' '}
                {toggleId && (
                  <button
                    className="link-button"
                    disabled={togglingId === toggleId}
                    onClick={() => handleToggle(item)}
                  >
                    {togglingId === item.assignmentId
                      ? '...'
                      : isActive
                      ? 'Disable'
                      : 'Enable'}
                  </button>
                )}
              </li>
            );
          })}
        </ul>
      )}

      {onAssign && (
        <form className="entity-form" onSubmit={handleAssign}>
          <div className="form-row">
            <label>{selectLabel}</label>
            <select value={selected} onChange={(e) => setSelected(e.target.value)}>
              <option value="">Select...</option>
              {optionsList.map((o) => (
                <option key={o.id} value={o.id}>
                  {o.name}
                </option>
              ))}
            </select>
          </div>
          <button type="submit" disabled={busy || !selected}>
            {busy ? 'Assigning...' : 'Assign'}
          </button>
        </form>
      )}
    </div>
  );
}
