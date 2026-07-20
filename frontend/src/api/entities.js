import { api } from './client';

function makeEntityApi(basePath) {
  return {
    list: () => api.get(basePath),
    get: (id) => api.get(`${basePath}/${id}`),
    create: (data) => api.post(basePath, data),
    update: (id, data) => api.put(`${basePath}/${id}`, data),
    disable: (id) => api.patch(`${basePath}/${id}/disable`),
    enable: (id) => api.patch(`${basePath}/${id}/enable`),
  };
}

// Association APIs: create a link between two entities, read a single
// active assignment by its assignmentId, and toggle it on/off. Unlike
// makeEntityApi there is no list/update route — assignments are looked up
// through the owning entity (e.g. rolesApi.permissions) which returns each
// item's assignmentId for use with get/disable/enable here.
function makeAssociationApi(basePath) {
  return {
    create: (data) => api.post(basePath, data),
    get: (assignmentId) => api.get(`${basePath}/${assignmentId}`),
    disable: (assignmentId) => api.patch(`${basePath}/${assignmentId}/disable`),
    enable: (assignmentId) => api.patch(`${basePath}/${assignmentId}/enable`),
  };
}

export const actionsApi = makeEntityApi('/api/actions');
export const resourcesApi = makeEntityApi('/api/resources');
export const permissionsApi = makeEntityApi('/api/permissions'); // no PUT route on backend
export const rolesApi = {
  ...makeEntityApi('/api/roles'),
  children: (id) => api.get(`/api/roles/${id}/children`),
  permissions: (id) => api.get(`/api/roles/${id}/permissions`),
};
export const groupsApi = {
  ...makeEntityApi('/api/groups'),
  children: (id) => api.get(`/api/groups/${id}/children`),
  permissions: (id) => api.get(`/api/groups/${id}/permissions`),
};
export const subjectsApi = makeEntityApi('/api/subjects');

export const rolePermissionsApi = makeAssociationApi('/api/role-permissions');
export const groupPermissionsApi = makeAssociationApi('/api/group-permissions');
export const roleGroupsApi = makeAssociationApi('/api/role-groups');

function withAssignmentIds(request) {
  return request.then((assignments) =>
    (assignments || []).map((assignment) => ({ ...assignment, assignmentId: assignment.id }))
  );
}

rolePermissionsApi.listForRole = (roleId) => withAssignmentIds(api.get(`/api/role-permissions?roleId=${roleId}`));
groupPermissionsApi.listForGroup = (groupId) => withAssignmentIds(api.get(`/api/group-permissions?groupId=${groupId}`));
roleGroupsApi.listForRole = (roleId) => withAssignmentIds(api.get(`/api/role-groups?roleId=${roleId}`));
roleGroupsApi.listForGroup = (groupId) => withAssignmentIds(api.get(`/api/role-groups?groupId=${groupId}`));

function makeHierarchyApi(basePath, parentIdField, childIdField) {
  return {
    create: (parentId, childId) => api.post(basePath, {
      [parentIdField]: parentId,
      [childIdField]: childId,
    }),
    listForParent: (parentId) => api.get(`${basePath}?${parentIdField}=${parentId}`),
    disable: (parentId, childId) => api.patch(`${basePath}/${parentId}/${childId}/disable`),
    enable: (parentId, childId) => api.patch(`${basePath}/${parentId}/${childId}/enable`),
  };
}

export const roleHierarchiesApi = makeHierarchyApi(
  '/api/role-hierarchies',
  'parentRoleId',
  'childRoleId'
);
export const groupHierarchiesApi = makeHierarchyApi(
  '/api/group-hierarchies',
  'parentGroupId',
  'childGroupId'
);

export const subjectRolesApi = {
  list: (subjectId) => api.get(`/api/subjects/${subjectId}/roles`),
  assign: (subjectId, roleId) => api.post(`/api/subjects/${subjectId}/roles/${roleId}`),
  get: (assignmentId) => api.get(`/api/subject-roles/${assignmentId}`),
  assignments: (subjectId) => withAssignmentIds(api.get(`/api/subject-roles?subjectId=${subjectId}`)),
  disable: (assignmentId) => api.patch(`/api/subject-roles/${assignmentId}/disable`),
  enable: (assignmentId) => api.patch(`/api/subject-roles/${assignmentId}/enable`),
};
