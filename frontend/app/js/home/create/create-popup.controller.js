/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 (function() {
  'use strict';

  let _repositories, _$state, _$modalInstance, _artifacts;

  class CreatePopupController {
    constructor(repositories, $state, $modalInstance, artifactFactories, artifacts) {
      _repositories = repositories;
      _$state = $state;
      _$modalInstance = $modalInstance;
      _artifacts = artifacts;
      this.types = artifactFactories.getFactories();
      this.type = this.types.page;
    }

    isNameUniqueIfRelevantForType(name, type) {
      return type.hasUniqueName &&
          (_artifacts || [])
            .filter(item => item.type === type.key)
            .some(item => item.name === name);
    }

    create(type, name) {
      _repositories.get(type.key).create(type.create(name)).then(data =>
        _$state.go('designer.' + type.key, {
          id: data.id
        })).then(_$modalInstance.close);
    }
  }

  angular
    .module('bonitasoft.designer.home')
    .controller('CreatePopupController', CreatePopupController);
})();
