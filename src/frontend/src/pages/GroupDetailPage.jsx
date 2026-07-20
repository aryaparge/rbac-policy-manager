import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  groupsApi, permissionsApi, rolesApi, groupPermissionsApi, roleGroupsApi, groupHierarchiesApi,
} from '../api/entities';
import StatusBadge from '../components/StatusBadge';
import AssociationSection from '../components/AssociationSection';

export default function GroupDetailPage() {
  const { id } = useParams();
  const [group, setGroup] = useState(null);
  const [children, setChildren] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [allPermissions, setAllPermissions] = useState([]);
  const [allGroups, setAllGroups] = useState([]);
  const [allRoles, setAllRoles] = useState([]);
  const [childAssignments, setChildAssignments] = useState([]);
  const [permissionAssignments, setPermissionAssignments] = useState([]);
  // No "roles for this group" list route exists yet on the backend, so
  // role-group assignments created here are tracked locally for the
  // session (same limitation as on RoleDetailPage).
  const [roleAssignments, setRoleAssignments] = useState([]);
  const [error, setError] = useState('');

  async function load() {
    try {
      const [g, childLinks, permissionLinks, roleLinks, allPerms, allGrps, allRls] = await Promise.all([
        groupsApi.get(id),
        groupHierarchiesApi.listForParent(id),
        groupPermissionsApi.listForGroup(id),
        roleGroupsApi.listForGroup(id),
        permissionsApi.list(),
        groupsApi.list(),
        rolesApi.list(),
      ]);
      setGroup(g);
      setChildAssignments(childLinks || []);
      setPermissionAssignments(permissionLinks || []);
      setRoleAssignments(roleLinks || []);
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
    await groupPermissionsApi.create({ groupId: id, permissionId });
    load();
  }

  async function handleTogglePermission(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await groupPermissionsApi.disable(item.assignmentId);
    else await groupPermissionsApi.enable(item.assignmentId);
    load();
  }

  async function handleAssignRole(roleId) {
    await roleGroupsApi.create({ roleId, groupId: id });
    load();
  }

  async function handleToggleRole(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await roleGroupsApi.disable(item.assignmentId);
    else await roleGroupsApi.enable(item.assignmentId);
    load();
  }

  async function handleAssignChildGroup(childGroupId) {
    await groupHierarchiesApi.create(id, childGroupId);
    load();
  }

  async function handleToggleChildGroup(item) {
    const isActive = (item.status || '').toUpperCase() === 'ACTIVE';
    if (isActive) await groupHierarchiesApi.disable(id, item.childGroupId);
    else await groupHierarchiesApi.enable(id, item.childGroupId);
    load();
  }

  if (error) return <div className="error-banner">{error}</div>;
  if (!group) return <p>Loading...</p>;

  const roleNameById = Object.fromEntries(allRoles.map((r) => [r.id, r.name]));

  return (
    <div className="page">
      <h2>
        {group.name} <StatusBadge status={group.status} />
      </h2>
      <p className="muted">{group.description}</p>

      <AssociationSection
        title="Child Groups"
        items={childAssignments}
        renderLabel={(a) => allGroups.find((g) => g.id === a.childGroupId)?.name || a.childGroupId}
        optionsList={allGroups.filter((g) => g.id !== id)}
        selectLabel="Child Group"
        onAssign={handleAssignChildGroup}
        onToggle={handleToggleChildGroup}
      />

      <AssociationSection
        title="Manage Direct Permissions"
        items={permissionAssignments}
        renderLabel={(p) => allPermissions.find((candidate) => candidate.id === p.permissionId)?.name || p.permissionId}
        optionsList={allPermissions}
        selectLabel="Assign Permission"
        onAssign={handleAssignPermission}
        onToggle={handleTogglePermission}
      />


      <AssociationSection
        title="Roles"
        items={roleAssignments}
        renderLabel={(a) => roleNameById[a.roleId] || a.roleId}
        optionsList={allRoles}
        selectLabel="Assign Role to Group"
        onAssign={handleAssignRole}
        onToggle={handleToggleRole}
      />
    </div>
  );
}
