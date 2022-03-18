import React from 'react';
import { Switch } from 'react-router-dom';

import ErrorBoundaryRoute from 'app/shared/error/error-boundary-route';

import Alert from './alert';
import AlertDetail from './alert-detail';
import AlertUpdate from './alert-update';
import AlertDeleteDialog from './alert-delete-dialog';

const Routes = ({ match }) => (
  <>
    <Switch>
      <ErrorBoundaryRoute exact path={`${match.url}/new`} component={AlertUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id/edit`} component={AlertUpdate} />
      <ErrorBoundaryRoute exact path={`${match.url}/:id`} component={AlertDetail} />
      <ErrorBoundaryRoute path={match.url} component={Alert} />
    </Switch>
    <ErrorBoundaryRoute exact path={`${match.url}/:id/delete`} component={AlertDeleteDialog} />
  </>
);

export default Routes;
