import { actionsApi, resourcesApi, permissionsApi, rolesApi, groupsApi, subjectsApi } from '../api/entities';

// Config-driven entity screens. Each entry describes list columns and the
// create-form fields, so EntityListPage.jsx can render Actions, Resources,
// Permissions, Roles, Groups, and Subjects from one generic component.
export const entityConfigs = {
  actions: {
    title: 'Actions',
    api: actionsApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
    ],
    createFields: [
      { name: 'name', label: 'Name', required: true },
      { name: 'description', label: 'Description' },
    ],
  },
  resources: {
    title: 'Resources',
    api: resourcesApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'displayName', label: 'Display Name' },
      { key: 'description', label: 'Description' },
    ],
    createFields: [
      { name: 'name', label: 'Name', required: true },
      { name: 'displayName', label: 'Display Name', required: true },
      { name: 'description', label: 'Description' },
    ],
  },
  permissions: {
    title: 'Permissions',
    api: permissionsApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
    ],
    createFields: [
      { name: 'actionId', label: 'Action', type: 'select', optionsFrom: 'actions', required: true },
      { name: 'resourceId', label: 'Resource', type: 'select', optionsFrom: 'resources', required: true },
      { name: 'description', label: 'Description' },
    ],
  },
  roles: {
    title: 'Roles',
    api: rolesApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
    ],
    createFields: [
      { name: 'name', label: 'Name', required: true },
      { name: 'description', label: 'Description' },
    ],
    detailPath: '/roles',
  },
  groups: {
    title: 'Groups',
    api: groupsApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'description', label: 'Description' },
    ],
    createFields: [
      { name: 'name', label: 'Name', required: true },
      { name: 'description', label: 'Description' },
    ],
    detailPath: '/groups',
  },
  subjects: {
    title: 'Subjects',
    api: subjectsApi,
    columns: [
      { key: 'name', label: 'Name' },
      { key: 'displayName', label: 'Display Name' },
      { key: 'subjectType', label: 'Type' },
    ],
    createFields: [
      { name: 'name', label: 'Name', required: true },
      { name: 'displayName', label: 'Display Name', required: true },
      {
        name: 'subjectType',
        label: 'Type',
        type: 'select',
        options: ['HUMAN', 'API_CLIENT', 'SYSTEM', 'SERVICE'],
        required: true,
      },
      { name: 'description', label: 'Description' },
    ],
    detailPath: '/subjects',
  },
};

// Entity types whose records can be used to populate <select> options
// inside another entity's create form (see "optionsFrom" above).
export const lookupApis = { actions: actionsApi, resources: resourcesApi };
