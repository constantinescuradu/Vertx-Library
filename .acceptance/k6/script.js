import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    {target: 20, duration: '10s'},
    {target: 15, duration: '15s'},
    {target:  0, duration: '5s'},
  ],
  thresholds: {
    http_req_duration: ['p(90) < 200', 'p(95) < 300'],
  },
};

export default function () {
  const res = http.get('http://localhost:8080/api');

  sleep(1);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response body is empty': (r) => r.json().length === 0,
  });
}
