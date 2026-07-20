import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { entityConfigs, lookupApis } from '../config/entityConfigs';
import StatusBadge from '../components/StatusBadge';

export default function EntityListPage() {
  const { entityType } = useParams();
  const config = entityConfigs[entityType];

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({});
  const [optionLists, setOptionLists] = useState({});
  const [submitting, setSubmitting] = useState(false);

  async function load() {
    setLoading(true);
    setError('');
    try {
      const data = await config.api.list();
      setItems(data || []);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!config) return;
    setShowForm(false);
    setFormData({});
    setError('');
    load();

    (async () => {
      const opts = {};
      for (const field of config.createFields) {
        if (field.optionsFrom) {
          try {
            opts[field.optionsFrom] = await lookupApis[field.optionsFrom].list();
          } catch (e) {
            // option list failures are non-fatal; the select just stays empty
          }
        }
      }
      setOptionLists(opts);
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [entityType]);

  async function handleCreate(e) {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await config.api.create(formData);
      setShowForm(false);
      setFormData({});
      load();
    } catch (e) {
      setError(e.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDisable(id) {
    if (!window.confirm('Disable this record?')) return;
    setError('');
    try {
      await config.api.disable(id);
      load();
    } catch (e) {
      setError(e.message);
    }
  }

  if (!config) return <p>Unknown entity type: {entityType}</p>;

  const filtered = items.filter((item) =>
    JSON.stringify(item).toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="page">
      <div className="page-header">
        <h2>{config.title}</h2>
        <div>
          <input
            className="search-input"
            placeholder="Search..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <button onClick={() => setShowForm((s) => !s)}>
            {showForm ? 'Cancel' : `+ New ${config.title.replace(/s$/, '')}`}
          </button>
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {showForm && (
        <form className="entity-form" onSubmit={handleCreate}>
          {config.createFields.map((field) => (
            <div className="form-row" key={field.name}>
              <label>
                {field.label}
                {field.required && ' *'}
              </label>
              {field.type === 'select' ? (
                <select
                  required={field.required}
                  value={formData[field.name] || ''}
                  onChange={(e) => setFormData({ ...formData, [field.name]: e.target.value })}
                >
                  <option value="" disabled>
                    Select...
                  </option>
                  {(field.optionsFrom ? optionLists[field.optionsFrom] || [] : field.options).map(
                    (opt) =>
                      field.optionsFrom ? (
                        <option key={opt.id} value={opt.id}>
                          {opt.name}
                        </option>
                      ) : (
                        <option key={opt} value={opt}>
                          {opt}
                        </option>
                      )
                  )}
                </select>
              ) : (
                <input
                  required={field.required}
                  value={formData[field.name] || ''}
                  onChange={(e) => setFormData({ ...formData, [field.name]: e.target.value })}
                />
              )}
            </div>
          ))}
          <button type="submit" disabled={submitting}>
            {submitting ? 'Creating...' : 'Create'}
          </button>
        </form>
      )}

      {loading ? (
        <p>Loading...</p>
      ) : (
        <table className="entity-table">
          <thead>
            <tr>
              {config.columns.map((col) => (
                <th key={col.key}>{col.label}</th>
              ))}
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((item) => (
              <tr key={item.id}>
                {config.columns.map((col) => (
                  <td key={col.key}>{item[col.key]}</td>
                ))}
                <td>
                  <StatusBadge status={item.status} />
                </td>
                <td>
                  {config.detailPath && <Link to={`${config.detailPath}/${item.id}`}>View</Link>}{' '}
                  <button className="link-button" onClick={() => handleDisable(item.id)}>
                    Disable
                  </button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={config.columns.length + 2} className="muted">
                  No records.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}
