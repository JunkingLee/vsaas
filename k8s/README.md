# Kubernetes 部署骨架

```bash
kubectl apply -f namespace.yaml
kubectl apply -f deployment-auth.yaml
```

建议：密钥用 Secret；探针 `/actuator/health`；多副本确保已建 `shedlock` 表。
