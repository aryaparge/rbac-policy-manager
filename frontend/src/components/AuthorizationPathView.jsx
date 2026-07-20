// Renders one AuthorizationPath as a left-to-right node timeline.
// Node types come from the backend PathNode.type enum: SUBJECT, ROLE,
// GROUP, ROLE_HIERARCHY, GROUP_HIERARCHY, PERMISSION.
const NODE_STYLES = {
  SUBJECT: { label: 'Subject', className: 'node-subject' },
  ROLE: { label: 'Role', className: 'node-role' },
  GROUP: { label: 'Group', className: 'node-group' },
  ROLE_HIERARCHY: { label: 'Role Hierarchy', className: 'node-role' },
  GROUP_HIERARCHY: { label: 'Group Hierarchy', className: 'node-group' },
  PERMISSION: { label: 'Permission', className: 'node-permission' },
};

export default function AuthorizationPathView({ path, index }) {
  return (
    <div className="path-card">
      <div className="path-title">Path {index + 1}</div>
      <div className="path-timeline">
        {path.nodes.map((node, i) => {
          const style = NODE_STYLES[node.type] || {};
          return (
            <div className="path-step" key={`${node.id}-${i}`}>
              <div
                className={`path-node ${style.className || ''}`}
                title={`${node.type}: ${node.name} (${node.id})`}
              >
                <div className="path-node-type">{style.label || node.type}</div>
                <div className="path-node-name">{node.name}</div>
              </div>
              {i < path.nodes.length - 1 && <div className="path-arrow">&rarr;</div>}
            </div>
          );
        })}
      </div>
    </div>
  );
}
