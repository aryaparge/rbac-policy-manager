export default function DecisionBadge({ decision }) {
  if (!decision) return null;
  const isAllow = decision === 'ALLOW';
  return <span className={`decision-badge ${isAllow ? 'allow' : 'deny'}`}>{decision}</span>;
}
