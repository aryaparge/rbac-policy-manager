import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  rolesApi, permissionsApi, groupsApi, rolePermissionsApi, roleGroupsApi, roleHierarchiesApi,
} from '../api/entities';
import StatusBadge from '../components/StatusBadge';
import AssociationSection from '../components/AssociationSection';

export default function RoleDetailPage() {
  const { id } = useParams();
  const [role, setRole] = useState(null);
  const [children, setChildren] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [allPermissions, setAllPermissions] = useState([]);
  const [allGroups, setAllGroups] = useState([]);
  const [allRoles, setAllRoles] = useState([]);
  const [childAssignments, setChildAssignments] = useState([]);
  const [permissionAssignments, setPermissionAssignments] = useState([]);
  // The API only exposes create/read-one/disable/enable for role-groups —
  // there's no "list groups for this role" route yet, so we track
  // assignments created in this session locally. They'll disappear on
  // reload until a list endpoint is added on the backend.
  const [groupAssignments, setGroupAssignments] = useState([]);
  const [error, setError] = useState('');

  async function load() {
    try {
      const [r, childLinks, permissionLinks, groupLinks, allPerms, allGrps, allRls] = await Promise.all([
        rolesApi.get(id),
        roleHierarchiesApi.listForParent(id),
        rolePermissionsApi.listForRole(id),
        roleGroupsApi.listForRole(id),
        permissionsApi.list(),
        groupsApi.list(),
        rolesApi.list(),
      ]);
      setRole(r);
      setChildAssignments(childLinks || []);
      setPermissionAssignments(permissionLinks || []);
      setGroupAssignments(groupLinks || []);
      setAllPermissions(allPerms || []);
      setAllGroups(allGrps || []);
      setAllRoles(allRls || []);
    } catch (e) {
      setError(e.message);
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  async function handleAssignPermission(permissionId) {
    await rolePermissionsApi.create({ roleId: id, permissionId });
    load();
  }

  async function handleTogglePermission(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await rolePermissionsApi.disable(item.assignmentId);
    else await rolePermissionsApi.enable(item.assignmentId);
    load();
  }

  async function handleAssignGroup(groupId) {
    await roleGroupsApi.create({ roleId: id, groupId });
    load();
  }

  async function handleToggleGroup(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await roleGroupsApi.disable(item.assignmentId);
    else await roleGroupsApi.enable(item.assignmentId);
    load();
  }

  async function handleAssignChildRole(childRoleId) {
    await roleHierarchiesApi.create(id, childRoleId);
    load();
  }

  async function handleToggleChildRole(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await roleHierarchiesApi.disable(id, item.childRoleId);
    else await roleHierarchiesApi.enable(id, item.childRoleId);
    load();
  }

  if (error) return <div className="error-banner">{error}</div>;
  if (!role) return <p>Loading...</p>;

  const groupNameById = Object.fromEntries(allGroups.map((g) => [g.id, g.name]));

  return (
    <div className="page">
      <h2>
        {role.name} <StatusBadge status={role.status} />
      </h2>
      <p className="muted">{role.description}</p>

      <AssociationSection
        title="Child Roles"
        items={childAssignments}
        renderLabel={(a) => allRoles.find((r) => r.id === a.childRoleId)?.name || a.childRoleId}
        optionsList={allRoles.filter((r) => r.id !== id)}
        selectLabel="Child Role"
        onAssign={handleAssignChildRole}
        onToggle={handleToggleChildRole}
      />

      <AssociationSection
        title="Manage Direct Permissions"
        items={permissionAssignments}
        renderLabel={(p) => {
          const permission = allPermissions.find((candidate) => candidate.id === p.permissionId);
          return permission?.name || p.permissionId;
        }}
        optionsList={allPermissions}
        selectLabel="Assign Permission"
        onAssign={handleAssignPermission}
        onToggle={handleTogglePermission}
      />


      <AssociationSection
        title="Groups"
        items={groupAssignments}
        renderLabel={(a) => groupNameById[a.groupId] || a.groupId}
        optionsList={allGroups}
        selectLabel="Assign to Group"
        onAssign={handleAssignGroup}
        onToggle={handleToggleGroup}
      />
    </div>
  );
}
