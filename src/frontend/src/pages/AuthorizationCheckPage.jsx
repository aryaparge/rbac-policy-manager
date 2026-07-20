import { useEffect, useState } from 'react';
import { subjectsApi, resourcesApi, actionsApi } from '../api/entities';
import { checkAuthorization } from '../api/authorization';
import DecisionBadge from '../components/DecisionBadge';
import AuthorizationPathView from '../components/AuthorizationPathView';

export default function AuthorizationCheckPage() {
  const [subjects, setSubjects] = useState([]);
  const [resources, setResources] = useState([]);
  const [actions, setActions] = useState([]);

  const [subjectId, setSubjectId] = useState('');
  const [resourceId, setResourceId] = useState('');
  const [actionId, setActionId] = useState('');

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const [s, r, a] = await Promise.all([
          subjectsApi.list(),
          resourcesApi.list(),
          actionsApi.list(),
        ]);
        setSubjects(s || []);
        setResources(r || []);
        setActions(a || []);
      } catch (e) {
        setError(e.message);
      }
    })();
  }, []);

  async function handleCheck(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await checkAuthorization(subjectId, resourceId, actionId);
      setResult(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  const canCheck = subjectId && resourceId && actionId;

  return (
    <div className="page">
      <h2>Authorization Check</h2>
      <p className="muted">
        Select a subject, resource, and action to evaluate access and see the permission evidence
        path that produced the decision.
      </p>

      {error && <div className="error-banner">{error}</div>}

      <form className="auth-check-form" onSubmit={handleCheck}>
        <div className="form-row">
          <label>Subject</label>
          <select value={subjectId} onChange={(e) => setSubjectId(e.target.value)}>
            <option value="">Select subject...</option>
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.displayName} ({s.name})
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label>Resource</label>
          <select value={resourceId} onChange={(e) => setResourceId(e.target.value)}>
            <option value="">Select resource...</option>
            {resources.map((r) => (
              <option key={r.id} value={r.id}>
                {r.displayName}
              </option>
            ))}
          </select>
        </div>
        <div className="form-row">
          <label>Action</label>
          <select value={actionId} onChange={(e) => setActionId(e.target.value)}>
            <option value="">Select action...</option>
            {actions.map((a) => (
              <option key={a.id} value={a.id}>
                {a.name}
              </option>
            ))}
          </select>
        </div>
        <button type="submit" disabled={!canCheck || loading}>
          {loading ? 'Checking...' : 'Check Authorization'}
        </button>
      </form>

      {result && (
        <div className="auth-result">
          <div className="auth-result-header">
            <span>Decision:</span>
            <DecisionBadge decision={result.decision} />
          </div>

          {result.decision === 'ALLOW' ? (
            <div className="path-list">
              {result.paths.map((path, i) => (
                <AuthorizationPathView key={i} path={path} index={i} />
              ))}
            </div>
          ) : (
            <p className="muted deny-empty-state">
              No permission path was found granting this subject the selected action on this
              resource.
            </p>
          )}
        </div>
      )}
    </div>
  );
}
