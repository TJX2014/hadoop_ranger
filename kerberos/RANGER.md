支持:
grant select ON TABLE test13.aa1 to USER xiaoming with grant option
revoke select ON TABLE test13.aa1 from USER xiaoming

不支持:
revoke select ON URI "/tmp/111" FROM USER xiaoming;