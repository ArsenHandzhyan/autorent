import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Switch, message } from 'antd';
import { EditOutlined, DollarOutlined } from '@ant-design/icons';

const AccountManagement = () => {
    const [accounts, setAccounts] = useState([]);
    const [loading, setLoading] = useState(false);
    const [modalVisible, setModalVisible] = useState(false);
    const [selectedAccount, setSelectedAccount] = useState(null);
    const [form] = Form.useForm();

    const fetchAccounts = async () => {
        setLoading(true);
        try {
            const response = await fetch('/api/accounts');
            const data = await response.json();
            setAccounts(data);
        } catch (error) {
            message.error('Ошибка при загрузке счетов');
        }
        setLoading(false);
    };

    useEffect(() => {
        fetchAccounts();
    }, []);

    const handleUpdateBalance = async (values) => {
        try {
            const response = await fetch(`/api/accounts/${selectedAccount.id}/balance`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ amount: values.amount })
            });
            if (response.ok) {
                message.success('Баланс успешно обновлен');
                setModalVisible(false);
                fetchAccounts();
            }
        } catch (error) {
            message.error('Ошибка при обновлении баланса');
        }
    };

    const handleUpdateCreditLimit = async (values) => {
        try {
            const response = await fetch(`/api/accounts/${selectedAccount.id}/credit-limit`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ creditLimit: values.creditLimit })
            });
            if (response.ok) {
                message.success('Кредитный лимит успешно обновлен');
                setModalVisible(false);
                fetchAccounts();
            }
        } catch (error) {
            message.error('Ошибка при обновлении кредитного лимита');
        }
    };

    const handleToggleNegativeBalance = async (accountId, allow) => {
        try {
            const response = await fetch(`/api/accounts/${accountId}/allow-negative`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ allow })
            });
            if (response.ok) {
                message.success('Настройки отрицательного баланса обновлены');
                fetchAccounts();
            }
        } catch (error) {
            message.error('Ошибка при обновлении настроек');
        }
    };

    const columns = [
        {
            title: 'Пользователь',
            dataIndex: ['user', 'username'],
            key: 'username',
        },
        {
            title: 'Баланс',
            dataIndex: 'balance',
            key: 'balance',
            render: (balance) => `${balance} ₽`
        },
        {
            title: 'Кредитный лимит',
            dataIndex: 'creditLimit',
            key: 'creditLimit',
            render: (limit) => `${limit} ₽`
        },
        {
            title: 'Разрешен отрицательный баланс',
            dataIndex: 'allowNegativeBalance',
            key: 'allowNegativeBalance',
            render: (allow, record) => (
                <Switch
                    checked={allow}
                    onChange={(checked) => handleToggleNegativeBalance(record.id, checked)}
                />
            )
        },
        {
            title: 'Действия',
            key: 'actions',
            render: (_, record) => (
                <>
                    <Button
                        icon={<DollarOutlined />}
                        onClick={() => {
                            setSelectedAccount(record);
                            form.setFieldsValue({ amount: 0 });
                            setModalVisible(true);
                        }}
                        style={{ marginRight: 8 }}
                    >
                        Изменить баланс
                    </Button>
                    <Button
                        icon={<EditOutlined />}
                        onClick={() => {
                            setSelectedAccount(record);
                            form.setFieldsValue({ creditLimit: record.creditLimit });
                            setModalVisible(true);
                        }}
                    >
                        Изменить лимит
                    </Button>
                </>
            )
        }
    ];

    return (
        <div className="account-management">
            <h2>Управление счетами</h2>
            <Table
                columns={columns}
                dataSource={accounts}
                loading={loading}
                rowKey="id"
            />
            <Modal
                title={selectedAccount?.creditLimit !== undefined ? "Изменить кредитный лимит" : "Изменить баланс"}
                visible={modalVisible}
                onCancel={() => setModalVisible(false)}
                footer={null}
            >
                <Form
                    form={form}
                    onFinish={selectedAccount?.creditLimit !== undefined ? handleUpdateCreditLimit : handleUpdateBalance}
                >
                    <Form.Item
                        name={selectedAccount?.creditLimit !== undefined ? "creditLimit" : "amount"}
                        label={selectedAccount?.creditLimit !== undefined ? "Кредитный лимит" : "Сумма"}
                        rules={[{ required: true, message: 'Пожалуйста, введите значение' }]}
                    >
                        <Input type="number" />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit">
                            Сохранить
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default AccountManagement; 