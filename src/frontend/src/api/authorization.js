import { api } from './client';

export function checkAuthorization(subjectId, resourceId, actionId) {
  return api.post('/api/authorization/check', { subjectId, resourceId, actionId });
}
