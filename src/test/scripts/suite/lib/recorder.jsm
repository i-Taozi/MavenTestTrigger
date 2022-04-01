/*
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
// TODO: Use Reflect.global when specified
const global = System.global;

const {
  Object, String, Proxy, Reflect
} = global;

const {
  raw: String_raw,
} = String;

function mixin(target, source) {
  for (let name of Reflect.ownKeys(source)) {
    Reflect.defineProperty(target, name, Reflect.getOwnPropertyDescriptor(source, name));
  }
  return target;
}

const $Reflect = mixin(Object.create(null), Reflect);

function PropertyKeyToString(propertyKey) {
  return String(propertyKey);
}

export function createLogger() {
  let out = "";
  return {log(...args) { out += String_raw(...args) }, output: () => out};
}

export function createObject(target, {log, output} = createLogger()) {
  function trap(name, target, propertyKey, ...args) {
    log `${PropertyKeyToString(propertyKey)};`;
    return $Reflect[name](target, propertyKey, ...args);
  }
  let object = new Proxy(target, new Proxy({
    getOwnPropertyDescriptor(target, propertyKey) {
      return trap("getOwnPropertyDescriptor", target, propertyKey);
    },
    defineProperty(target, propertyKey, descriptor) {
      return trap("defineProperty", target, propertyKey, descriptor);
    },
    has(target, propertyKey) {
      return trap("has", target, propertyKey);
    },
    get(target, propertyKey, receiver) {
      return trap("get", target, propertyKey, receiver);
    },
    set(target, propertyKey, value, receiver) {
      return trap("set", target, propertyKey, value, receiver);
    },
    deleteProperty(target, propertyKey) {
      return trap("deleteProperty", target, propertyKey);
    },
  }, {
    get(t, p, r) {
      log `${p}:`;
      return $Reflect.get(t, p, r);
    }
  }));
  return {object, record: output};
}

const watchedObjects = new WeakSet();

export function unwatch(object) {
  watchedObjects.delete(object);
}

export function watch(target, history) {
  function trap(entry, name, ...args) {
    if (!watchedObjects.has(object)) {
      return $Reflect[name](...args);
    }
    try {
      let result = $Reflect[name](...args);
      history.push(Object.assign(entry, {name, result}));
      return result;
    } catch (exception) {
      history.push(Object.assign(entry, {name, exception}));
      throw exception;
    }
  }
  let object = new Proxy(target, new Proxy({
    getPrototypeOf(target) {
      return trap({target}, "getPrototypeOf", target);
    },
    setPrototypeOf(target, value) {
      return trap({target, value}, "setPrototypeOf", target, value);
    },
    isExtensible(target) {
      return trap({target}, "isExtensible", target);
    },
    preventExtensions(target) {
      return trap({target}, "preventExtensions", target);
    },
    getOwnPropertyDescriptor(target, property) {
      return trap({target, property}, "getOwnPropertyDescriptor", target, property);
    },
    defineProperty(target, property, descriptor) {
      return trap({target, property, descriptor}, "defineProperty", target, property, descriptor);
    },
    has(target, property) {
      return trap({target, property}, "has", target, property);
    },
    get(target, property, receiver) {
      return trap({target, property, receiver}, "get", target, property, receiver);
    },
    set(target, property, value, receiver) {
      return trap({target, property, value, receiver}, "set", target, property, value, receiver);
    },
    deleteProperty(target, property) {
      return trap({target, property}, "deleteProperty", target, property);
    },
    enumerate(target) {
      return trap({target}, "enumerate", target);
    },
    ownKeys(target) {
      return trap({target}, "ownKeys", target);
    },
    apply(target, thisArgument, argumentsList) {
      return trap({target, thisArgument, argumentsList}, "apply", target, thisArgument, argumentsList);
    },
    construct(target, argumentsList) {
      return trap({target, argumentsList}, "construct", target, argumentsList);
    },
  }, {
    get(t, p, r) {
      if (p in t) {
        return $Reflect.get(t, p, r);
      }
      // unknown trap?
      history.push({name: p});
    }
  }));
  watchedObjects.add(object);
  return object;
}
